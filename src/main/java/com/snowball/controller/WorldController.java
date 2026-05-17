package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.dto.WorldCreateDTO;
import com.snowball.service.WorldService;
import com.snowball.vo.WorldVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/worlds")
public class WorldController extends BaseController {

    private final WorldService worldService;

    public WorldController(WorldService worldService) {
        this.worldService = worldService;
    }

    @GetMapping("/{id}")
    public Result<WorldVO> getWorld(@PathVariable Long id) {
        return Result.success(worldService.getWorldById(id));
    }

    @GetMapping
    public Result<List<WorldVO>> getMyWorlds() {
        Long userId = getOptionalUserId();
        if (userId == null) {
            return Result.success(Collections.emptyList());
        }
        return Result.success(worldService.getMyWorlds(userId));
    }

    @PostMapping
    public Result<WorldVO> createWorld(@Valid @RequestBody WorldCreateDTO dto) {
        Long userId = getOptionalUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(worldService.createWorld(userId, dto));
    }
}
