package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.service.FileStorageService;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;
import javax.imageio.ImageIO;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket:snowball}")
    private String bucket;

    @Value("${app.upload.allowed-extensions:jpg,jpeg,png,gif,webp,bmp}")
    private String allowedExtensions;

    private static final Logger log = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    private MinioClient client;

    @PostConstruct
    public void init() {
        try {
            client = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
            boolean bucketExists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (bucketExists) {
                log.info("MinIO 连接成功 — endpoint: {}, bucket: {}", endpoint, bucket);
            } else {
                log.warn("MinIO bucket '{}' 不存在，首次上传时将自动创建", bucket);
            }
        } catch (Exception e) {
            log.error("MinIO 连接失败 — endpoint: {}, 原因: {}", endpoint, e.getMessage());
            throw new RuntimeException("MinIO客户端初始化失败", e);
        }
    }

    @Override
    public String saveFile(MultipartFile file, String subdirectory, String prefix, Long ownerId) {
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        Set<String> allowed = new HashSet<>(Arrays.asList(allowedExtensions.split(",")));
        String extWithoutDot = ext.startsWith(".") ? ext.substring(1) : ext;
        if (!extWithoutDot.isEmpty() && !allowed.contains(extWithoutDot)) {
            throw new BusinessException(400, "不支持的文件类型: " + extWithoutDot);
        }

        try {
            byte[] fileBytes = file.getBytes();

            // SHA-256 内容哈希 → 相同文件只存一份
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String hashHex = bytesToHex(digest.digest(fileBytes));
            String objectName = subdirectory + "/" + hashHex + ext;

            ensureBucket();

            // 已存在则跳过上传（缩略图也已存在则跳过）
            boolean exists = objectExists(objectName);
            if (!exists) {
                client.putObject(PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .stream(new ByteArrayInputStream(fileBytes), fileBytes.length, -1)
                        .contentType(file.getContentType())
                        .build());
                generateAndSaveThumbnail(fileBytes, ext, objectName);
            }

            return "/api/v1/files/" + bucket + "/" + objectName;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(500, "文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> saveFiles(List<MultipartFile> files, String subdirectory, String prefix, Long ownerId) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(saveFile(file, subdirectory, prefix, ownerId));
        }
        return urls;
    }

    @Override
    public void deleteFile(String url) {
        try {
            String prefix = "/api/v1/files/" + bucket + "/";
            String objectName = url.startsWith(prefix)
                    ? url.substring(prefix.length())
                    : url.replaceFirst("^/" + bucket + "/", "");
            client.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            // silently ignore
        }
    }

    private static final int THUMB_MAX_WIDTH = 400;

    private void generateAndSaveThumbnail(byte[] originalBytes, String ext, String objectName) {
        String lowerExt = ext.toLowerCase();
        if (!Set.of(".jpg", ".jpeg", ".png", ".bmp", ".gif").contains(lowerExt)) {
            return;
        }
        try {
            BufferedImage original = ImageIO.read(new ByteArrayInputStream(originalBytes));
            if (original == null) return;

            int w = original.getWidth();
            int h = original.getHeight();
            if (w <= THUMB_MAX_WIDTH) return;

            int newH = (int) (h * ((double) THUMB_MAX_WIDTH / w));
            Image scaled = original.getScaledInstance(THUMB_MAX_WIDTH, newH, Image.SCALE_SMOOTH);
            BufferedImage thumb = new BufferedImage(THUMB_MAX_WIDTH, newH, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = thumb.createGraphics();
            g.drawImage(scaled, 0, 0, null);
            g.dispose();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(thumb, "jpg", bos);
            byte[] thumbBytes = bos.toByteArray();

            String thumbName = objectName.replaceAll("\\.[^.]+$", "_thumb.jpg");
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(thumbName)
                    .stream(new ByteArrayInputStream(thumbBytes), thumbBytes.length, -1)
                    .contentType("image/jpeg")
                    .build());
        } catch (Exception e) {
            log.warn("缩略图生成失败: {}", e.getMessage());
        }
    }

    private boolean objectExists(String objectName) {
        try {
            client.statObject(StatObjectArgs.builder().bucket(bucket).object(objectName).build());
            return true;
        } catch (ErrorResponseException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private void ensureBucket() {
        try {
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception ignored) {
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
