package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.service.FileStorageService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class UploadController extends BaseController {

    private final FileStorageService fileStorageService;

    public UploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        Long userId = getCurrentUserId();
        String url = fileStorageService.saveFile(file, "images", "img", userId);
        return Result.success(Map.of("url", url));
    }

    @PostMapping("/upload/multiple")
    public Result<Map<String, Object>> uploadMultiple(@RequestParam("files") List<MultipartFile> files) {
        Long userId = getCurrentUserId();
        List<String> urls = fileStorageService.saveFiles(files, "images", "img", userId);
        return Result.success(Map.of("urls", urls));
    }
}
