package com.example.snowball.controller;
import com.example.snowball.common.Result;
import com.example.snowball.dto.UserLoginDTO;
import com.example.snowball.dto.UserRegisterDTO;
import com.example.snowball.service.UserService;
import com.example.snowball.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserService userService;
    public AuthController(UserService userService) { this.userService = userService; }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody UserRegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody UserLoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @GetMapping("/me") // 获取当前登录用户信息
    public Result<UserVO> me(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal(); // 从 SecurityContext 拿 ID
        return Result.success(userService.getCurrentUser(userId));
    }
}
