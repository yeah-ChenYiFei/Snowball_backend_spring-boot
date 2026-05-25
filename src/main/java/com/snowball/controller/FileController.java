package com.snowball.controller;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.io.OutputStream;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket:snowball}")
    private String bucket;

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

    @GetMapping("/**")
    public void getFile(@RequestParam(value = "thumb", required = false) String thumb,
                        HttpServletResponse response) {
        String path = (String) request.getAttribute(
                org.springframework.web.servlet.HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String fullPath = path.substring("/api/v1/files/".length());
        String objectName = fullPath.startsWith(bucket + "/")
                ? fullPath.substring(bucket.length() + 1)
                : fullPath;

        // Thumbnail: serve _thumb.jpg variant
        if ("1".equals(thumb)) {
            objectName = objectName.replaceAll("\\.[^.]+$", "_thumb.jpg");
        }

        try (InputStream is = client.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .build())) {

            // ETag from MinIO stat
            try {
                var stat = client.statObject(
                        io.minio.StatObjectArgs.builder().bucket(bucket).object(objectName).build());
                response.setHeader("ETag", "\"" + stat.etag() + "\"");
            } catch (Exception ignored) {
            }

            String contentType = deriveContentType(objectName);
            response.setContentType(contentType != null ? contentType : "application/octet-stream");
            response.setHeader("Cache-Control", "public, max-age=86400");

            OutputStream os = response.getOutputStream();
            is.transferTo(os);
            os.flush();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private String deriveContentType(String name) {
        String lower = name.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".bmp")) return "image/bmp";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        return "application/octet-stream";
    }

    private final jakarta.servlet.http.HttpServletRequest request;

    public FileController(jakarta.servlet.http.HttpServletRequest request) {
        this.request = request;
    }
}
