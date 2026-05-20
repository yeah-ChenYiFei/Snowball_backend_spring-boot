package com.snowball.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface FileStorageService {
    String saveFile(MultipartFile file, String subdirectory, String prefix, Long ownerId);
    List<String> saveFiles(List<MultipartFile> files, String subdirectory, String prefix, Long ownerId);
    void deleteFile(String url);
}
