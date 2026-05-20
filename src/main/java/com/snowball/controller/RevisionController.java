package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.dto.RevisionCreateDTO;
import com.snowball.service.RevisionService;
import com.snowball.vo.RevisionVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class RevisionController extends BaseController {

    private final RevisionService revisionService;

    public RevisionController(RevisionService revisionService) {
        this.revisionService = revisionService;
    }

    @GetMapping("/posts/{postId}/revisions")
    public Result<List<RevisionVO>> getRevisions(@PathVariable Long postId) {
        return Result.success(revisionService.getRevisions(postId));
    }

    @PostMapping("/posts/{postId}/revisions")
    public Result<RevisionVO> createRevision(@PathVariable Long postId, @Valid @RequestBody RevisionCreateDTO dto) {
        return Result.success(revisionService.createRevision(postId, getCurrentUserId(), dto));
    }
}
