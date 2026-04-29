package com.example.snowball.controller;

import com.example.snowball.common.Result;
import com.example.snowball.dto.PostCreateDTO;
import com.example.snowball.dto.PostUpdateDTO;
import com.example.snowball.entity.PostVersion;
import com.example.snowball.service.PostService;
import com.example.snowball.vo.PostDetailVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController extends BaseController{
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of("time", java.time.LocalDateTime.now().toString(), "message", "pong");
    }

    @GetMapping
    public Result<List<PostDetailVO>> getAllPosts() {
        Long userId = getOptionalUserId();
        return Result.success(postService.getAllPosts(userId));
    }



    @GetMapping("/{id}")
    public Result<PostDetailVO> getPostById(@PathVariable Long id) {
        return Result.success(postService.getPostById(id));
    }

    @PostMapping
    public Result<PostDetailVO> createPost(@RequestBody PostCreateDTO dto) {
        return Result.success(postService.createPost(getCurrentUserId(), dto));
    }

    @PutMapping("/{id}")
    public Result<PostDetailVO> updatePost(@PathVariable Long id, @RequestBody PostUpdateDTO dto) {
        return Result.success(postService.updatePost(id, getCurrentUserId(), dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id, getCurrentUserId());
        return Result.success();
    }

    @GetMapping("/{id}/versions")
    public Result<List<PostVersion>> getVersions(@PathVariable Long id) {
        return Result.success(postService.getPostVersions(id));
    }

    @PostMapping("/{id}/versions/{verId}/rollback")
    public Result<PostDetailVO> rollbackPost(@PathVariable Long id, @PathVariable Long verId) {
        return Result.success(postService.rollbackPost(id, verId, getCurrentUserId()));
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public Result<Void> adminDeletePost(@PathVariable Long id) {
        postService.forceDeletePost(id);
        return Result.success();
    }

    /**
     * 赞/踩/取消
     */
    @PostMapping("/{id}/react")
    public Result<Void> react(@PathVariable Long id, @RequestParam String reactionType) {
        // ✅ 修复：去掉了 @AuthenticationPrincipal，统一用 getCurrentUserId()
        postService.react(id, getCurrentUserId(), reactionType);
        return Result.success();
    }
}
