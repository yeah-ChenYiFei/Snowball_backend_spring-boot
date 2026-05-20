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
    public void getFile(HttpServletResponse response) {
        String path = (String) request.getAttribute(
                org.springframework.web.servlet.HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String fullPath = path.substring("/api/v1/files/".length());
        String objectName = fullPath.startsWith(bucket + "/")
                ? fullPath.substring(bucket.length() + 1)
                : fullPath;

        try (InputStream is = client.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .build())) {

            String contentType = java.nio.file.Files.probeContentType(
                    java.nio.file.Path.of(objectName));
            response.setContentType(contentType != null ? contentType : "application/octet-stream");
            response.setHeader("Cache-Control", "public, max-age=86400");

            OutputStream os = response.getOutputStream();
            is.transferTo(os);
            os.flush();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private final jakarta.servlet.http.HttpServletRequest request;

    public FileController(jakarta.servlet.http.HttpServletRequest request) {
        this.request = request;
    }
}
