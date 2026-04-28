package com.example.snowball.controller;
import com.example.snowball.common.Result;
import com.example.snowball.entity.Revision;
import com.example.snowball.repository.RevisionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/v1")
public class RevisionController {
    private final RevisionRepository revisionRepository;
    public RevisionController(RevisionRepository revisionRepository) { this.revisionRepository = revisionRepository; }

    @GetMapping("/posts/{postId}/revisions")
    public Result<List<Revision>> getRevisions(@PathVariable Long postId) {
        return Result.success(revisionRepository.findByOriginalPostIdOrderByCreatedAtDesc(postId));
    }

    @PostMapping("/posts/{postId}/revisions")
    public Result<Revision> createRevision(@PathVariable Long postId, @RequestBody Map<String, String> body, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Revision rev = new Revision();
        rev.setOriginalPostId(postId);
        rev.setAuthorUserId(userId);
        rev.setTitle(body.get("title"));
        rev.setBody(body.get("body"));
        rev.setSummary(body.get("summary"));
        return Result.success(revisionRepository.save(rev));
    }
}
