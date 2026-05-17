package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.dto.WorldEntryCreateDTO;
import com.snowball.service.WorldEntryService;
import com.snowball.vo.WorldEntryVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/worlds/{worldId}/entries")
public class WorldEntryController extends BaseController {

    private final WorldEntryService entryService;

    public WorldEntryController(WorldEntryService entryService) {
        this.entryService = entryService;
    }

    @GetMapping
    public Result<List<WorldEntryVO>> getEntries(
            @PathVariable Long worldId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search) {
        return Result.success(entryService.getEntries(worldId, type, search));
    }

    @GetMapping("/types")
    public Result<List<String>> getEntryTypes(@PathVariable Long worldId) {
        return Result.success(entryService.getEntryTypes(worldId));
    }

    @GetMapping("/{entryId}")
    public Result<WorldEntryVO> getEntry(@PathVariable Long worldId, @PathVariable Long entryId) {
        return Result.success(entryService.getEntry(entryId));
    }

    @PostMapping
    public Result<WorldEntryVO> createEntry(
            @PathVariable Long worldId,
            @Valid @RequestBody WorldEntryCreateDTO dto) {
        Long userId = getOptionalUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(entryService.createEntry(worldId, userId, dto));
    }

    @PutMapping("/{entryId}")
    public Result<WorldEntryVO> updateEntry(
            @PathVariable Long worldId,
            @PathVariable Long entryId,
            @Valid @RequestBody WorldEntryCreateDTO dto) {
        Long userId = getOptionalUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(entryService.updateEntry(entryId, userId, dto));
    }
}
