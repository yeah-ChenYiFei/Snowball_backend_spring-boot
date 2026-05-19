package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.service.ChainService;
import com.snowball.service.UserService;
import com.snowball.vo.ChainVO;
import com.snowball.vo.UserProfileVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
