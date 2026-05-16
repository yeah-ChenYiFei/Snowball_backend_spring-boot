package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.dto.UserLoginDTO;
import com.snowball.dto.UserRegisterDTO;
import com.snowball.service.UserService;
import com.snowball.vo.UserLoginVO; // ✅ 引入 VO
import com.snowball.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController extends BaseController { // ✅ 1. 继承基类

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody UserRegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<UserLoginVO> login(@Valid @RequestBody UserLoginDTO dto) { // ✅ 2. 返回值改 VO
        return Result.success(userService.login(dto));
    }

    @GetMapping("/me")
    public Result<UserVO> me() { // ✅ 3. 删掉 Authentication 参数
        Long userId = getCurrentUserId(); // ✅ 4. 用基类方法拿 ID
        return Result.success(userService.getCurrentUser(userId));
    }
}
