package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.dto.ChangeEmailDTO;
import com.snowball.dto.ChangePasswordDTO;
import com.snowball.dto.ChangeUsernameDTO;
import com.snowball.dto.DeleteAccountDTO;
import com.snowball.dto.VerifyNewEmailDTO;
import com.snowball.security.RateLimit;
import com.snowball.service.ChainService;
import com.snowball.service.UserService;
import com.snowball.vo.ChainVO;
import com.snowball.vo.UserProfileVO;
import jakarta.validation.Valid;
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

    @PutMapping("/me/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        Long userId = getCurrentUserId();
        userService.changePassword(userId, dto.getOldPassword(), dto.getNewPassword());
        return Result.success();
    }

    @PutMapping("/me/username")
    public Result<Void> changeUsername(@Valid @RequestBody ChangeUsernameDTO dto) {
        Long userId = getCurrentUserId();
        userService.changeUsername(userId, dto.getUsername());
        return Result.success();
    }

    @RateLimit(maxAttempts = 3, timeWindowSeconds = 600)
    @PutMapping("/me/email")
    public Result<Void> changeEmail(@Valid @RequestBody ChangeEmailDTO dto) {
        Long userId = getCurrentUserId();
        userService.changeEmail(userId, dto.getNewEmail());
        return Result.success();
    }

    @RateLimit(maxAttempts = 5, timeWindowSeconds = 600)
    @PostMapping("/me/email/verify")
    public Result<Void> verifyNewEmail(@Valid @RequestBody VerifyNewEmailDTO dto) {
        Long userId = getCurrentUserId();
        userService.verifyNewEmail(userId, dto.getCode());
        return Result.success();
    }

    @RateLimit(maxAttempts = 2, timeWindowSeconds = 3600)
    @DeleteMapping("/me")
    public Result<Void> deleteAccount(@Valid @RequestBody DeleteAccountDTO dto) {
        Long userId = getCurrentUserId();
        userService.deleteAccount(userId, dto.getPassword());
        return Result.success();
    }
}
