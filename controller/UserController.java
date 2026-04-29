package com.example.snowball.controller;

import com.example.snowball.common.Result;
import com.example.snowball.service.UserService;
import com.example.snowball.vo.UserProfileVO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController extends BaseController { // ✅ 1. 继承基类

    private final UserService userService; // ✅ 2. 只留 UserService！删掉那三个 Repository！

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}/profile")
    public Result<UserProfileVO> getProfile(@PathVariable Long id) {
        // ✅ 3. 直接调 Service，完美拿到拆弹后的纯净 VO
        return Result.success(userService.getUserProfile(id));
    }
}
