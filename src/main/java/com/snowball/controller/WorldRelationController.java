package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.dto.WorldRelationCreateDTO;
import com.snowball.service.WorldRelationService;
import com.snowball.service.WorldService;
import com.snowball.vo.WorldRelationVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/worlds/{worldId}/relations")
public class WorldRelationController extends BaseController {

    private final WorldRelationService relationService;
    private final WorldService worldService;

    public WorldRelationController(WorldRelationService relationService, WorldService worldService) {
        this.relationService = relationService;
        this.worldService = worldService;
    }

    @GetMapping
    public Result<List<WorldRelationVO>> getRelations(@PathVariable Long worldId) {
        Long userId = getOptionalUserId();
        worldService.checkWorldAccess(worldId, userId);
        return Result.success(relationService.getRelations(worldId));
    }

    @PostMapping
    public Result<WorldRelationVO> createRelation(
            @PathVariable Long worldId,
            @Valid @RequestBody WorldRelationCreateDTO dto) {
        Long userId = getOptionalUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(relationService.createRelation(worldId, userId, dto));
    }

    @GetMapping("/{relationId}")
    public Result<WorldRelationVO> getRelation(@PathVariable Long worldId, @PathVariable Long relationId) {
        Long userId = getOptionalUserId();
        worldService.checkWorldAccess(worldId, userId);
        return Result.success(relationService.getRelation(relationId));
    }

    @PutMapping("/{relationId}")
    public Result<WorldRelationVO> updateRelation(
            @PathVariable Long worldId,
            @PathVariable Long relationId,
            @Valid @RequestBody WorldRelationCreateDTO dto) {
        Long userId = getOptionalUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(relationService.updateRelation(relationId, userId, dto));
    }

    @DeleteMapping("/{relationId}")
    public Result<Void> deleteRelation(@PathVariable Long worldId, @PathVariable Long relationId) {
        Long userId = getOptionalUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        relationService.deleteRelation(relationId, userId);
        return Result.success();
    }
}
