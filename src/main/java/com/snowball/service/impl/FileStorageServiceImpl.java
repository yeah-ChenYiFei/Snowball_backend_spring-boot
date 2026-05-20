package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.upload.base-dir:uploads}")
    private String baseDir;

    @Override
    public String saveFile(MultipartFile file, String subdirectory, String prefix, Long ownerId) {
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = prefix + "_" + ownerId + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;

        try {
            Path uploadPath = Path.of(baseDir, subdirectory);
            Files.createDirectories(uploadPath);
            Path targetPath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return "/" + baseDir + "/" + subdirectory + "/" + filename;
        } catch (IOException e) {
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
            Path filePath = Path.of(url.replaceFirst("^/", ""));
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // silently ignore
        }
    }
}
