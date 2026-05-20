package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.service.ChainService;
import com.snowball.service.UserService;
import com.snowball.vo.ChainVO;
import com.snowball.vo.UserProfileVO;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController extends BaseController {

    private final UserService userService;
    private final ChainService chainService;

    public UserController(UserService userService, ChainService chainService) {
        this.userService = userService;
        this.chainService = chainService;
    }

    @GetMapping("/{id}/profile")
    public Result<UserProfileVO> getProfile(@PathVariable Long id) {
        return Result.success(userService.getUserProfile(id));
    }

    @GetMapping("/{id}/chain-activities")
    public Result<List<ChainVO>> getChainActivities(@PathVariable Long id) {
        return Result.success(chainService.getUserChainActivities(id));
    }

    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = getCurrentUserId();
        String avatarUrl = userService.uploadAvatar(userId, file);
        return Result.success(Map.of("avatarUrl", avatarUrl));
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        userService.updateProfile(userId, body.get("signature"));
        return Result.success();
    }
}
