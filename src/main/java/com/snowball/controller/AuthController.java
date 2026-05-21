package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.dto.*;
import com.snowball.security.RateLimit;
import com.snowball.service.UserService;
import com.snowball.vo.UserLoginVO;
import com.snowball.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController extends BaseController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @RateLimit(maxAttempts = 3, timeWindowSeconds = 3600)
    @PostMapping("/register")
    public Result<Map<String, Long>> register(@Valid @RequestBody UserRegisterDTO dto) {
        Long userId = userService.register(dto);
        return Result.success(Map.of("userId", userId));
    }

    @RateLimit(maxAttempts = 5, timeWindowSeconds = 600)
    @PostMapping("/verify-email")
    public Result<Void> verifyEmail(@RequestParam Long userId, @Valid @RequestBody VerifyEmailDTO dto) {
        userService.verifyEmail(userId, dto.getCode());
        return Result.success();
    }

    @RateLimit(maxAttempts = 10, timeWindowSeconds = 60)
    @PostMapping("/login")
    public Result<UserLoginVO> login(@Valid @RequestBody UserLoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @RateLimit(maxAttempts = 3, timeWindowSeconds = 600)
    @PostMapping("/forgot-password")
    public Result<Void> forgotPassword(@Valid @RequestBody ForgotPasswordDTO dto) {
        userService.forgotPassword(dto.getEmail());
        return Result.success();
    }

    @RateLimit(maxAttempts = 5, timeWindowSeconds = 600)
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        userService.resetPassword(dto.getEmail(), dto.getCode(), dto.getNewPassword());
        return Result.success();
    }

    @GetMapping("/me")
    public Result<UserVO> me() {
        Long userId = getCurrentUserId();
        return Result.success(userService.getCurrentUser(userId));
    }
}
