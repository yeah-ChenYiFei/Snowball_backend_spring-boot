package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.dto.WorldCreateDTO;
import com.snowball.dto.WorldUpdateDTO;
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
        Long userId = getOptionalUserId();
        return Result.success(worldService.getWorldById(id, userId));
    }

    @GetMapping
    public Result<List<WorldVO>> getAccessibleWorlds() {
        Long userId = getOptionalUserId();
        if (userId == null) {
            return Result.success(Collections.emptyList());
        }
        return Result.success(worldService.getAccessibleWorlds(userId));
    }

    @PostMapping
    public Result<WorldVO> createWorld(@Valid @RequestBody WorldCreateDTO dto) {
        Long userId = getOptionalUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(worldService.createWorld(userId, dto));
    }

    @PutMapping("/{id}")
    public Result<WorldVO> updateWorld(@PathVariable Long id, @RequestBody WorldUpdateDTO dto) {
        Long userId = getOptionalUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(worldService.updateWorld(id, userId, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteWorld(@PathVariable Long id) {
        Long userId = getOptionalUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        worldService.deleteWorld(id, userId);
        return Result.success();
    }
}
