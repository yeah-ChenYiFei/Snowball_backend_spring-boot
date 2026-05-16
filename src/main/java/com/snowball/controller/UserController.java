package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.service.UserService;
import com.snowball.vo.UserProfileVO;
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
