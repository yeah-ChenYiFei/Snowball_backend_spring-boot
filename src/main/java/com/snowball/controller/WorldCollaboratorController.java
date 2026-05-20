package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.dto.WorldCollaboratorAddDTO;
import com.snowball.service.WorldCollaboratorService;
import com.snowball.vo.CollaboratorVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/worlds/{worldId}/collaborators")
public class WorldCollaboratorController extends BaseController {

    private final WorldCollaboratorService collaboratorService;

    public WorldCollaboratorController(WorldCollaboratorService collaboratorService) {
        this.collaboratorService = collaboratorService;
    }

    @GetMapping
    public Result<List<CollaboratorVO>> getCollaborators(@PathVariable Long worldId) {
        return Result.success(collaboratorService.getCollaborators(worldId));
    }

    @PostMapping
    public Result<CollaboratorVO> addCollaborator(@PathVariable Long worldId,
                                                   @Valid @RequestBody WorldCollaboratorAddDTO dto) {
        return Result.success(collaboratorService.addCollaborator(worldId, getCurrentUserId(), dto.getFriendId()));
    }

    @DeleteMapping("/{userId}")
    public Result<String> removeCollaborator(@PathVariable Long worldId,
                                              @PathVariable Long userId) {
        collaboratorService.removeCollaborator(worldId, getCurrentUserId(), userId);
        return Result.success("已移除共创者");
    }
}
