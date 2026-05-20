package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.dto.WorldChangeActionDTO;
import com.snowball.service.WorldChangeService;
import com.snowball.vo.WorldChangeVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/worlds/{worldId}/changes")
public class WorldChangeController extends BaseController {

    private final WorldChangeService changeService;

    public WorldChangeController(WorldChangeService changeService) {
        this.changeService = changeService;
    }

    @GetMapping
    public Result<List<WorldChangeVO>> getPendingChanges(@PathVariable Long worldId) {
        return Result.success(changeService.getPendingChanges(worldId, getCurrentUserId()));
    }

    @PutMapping("/{changeId}/approve")
    public Result<WorldChangeVO> approveChange(@PathVariable Long worldId,
                                                @PathVariable Long changeId) {
        return Result.success(changeService.approveChange(changeId, worldId, getCurrentUserId()));
    }

    @PutMapping("/{changeId}/reject")
    public Result<WorldChangeVO> rejectChange(@PathVariable Long worldId,
                                               @PathVariable Long changeId,
                                               @Valid @RequestBody WorldChangeActionDTO dto) {
        return Result.success(changeService.rejectChange(changeId, worldId, getCurrentUserId(), dto.getRejectReason()));
    }
}
