package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.service.FileStorageService;
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

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

    private MinioClient client;

    @PostConstruct
    public void init() {
        try {
            client = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
        } catch (Exception e) {
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

        String objectName = subdirectory + "/" + prefix + "_" + ownerId + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;

        try (InputStream is = file.getInputStream()) {
            ensureBucket();
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(is, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            return "/api/v1/files/" + bucket + "/" + objectName;
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

    private void ensureBucket() {
        try {
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception ignored) {
        }
    }
}
