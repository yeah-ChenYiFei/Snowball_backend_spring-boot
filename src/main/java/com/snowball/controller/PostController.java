package com.snowball.controller;

import com.snowball.common.Result;
import jakarta.validation.Valid;
import com.snowball.dto.PostCreateDTO;
import com.snowball.dto.PostUpdateDTO;
import com.snowball.entity.PostVersion;
import com.snowball.service.PostService;
import com.snowball.vo.PostDetailVO;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public Result<List<PostDetailVO>> getAllPosts(@RequestParam(defaultValue = "hot") String sort) {
        Long userId = getOptionalUserId();
        return Result.success(postService.getAllPosts(userId, sort));
    }

    @GetMapping("/mine")
    public Result<List<PostDetailVO>> getMyPosts() {
        return Result.success(postService.getUserPosts(getCurrentUserId()));
    }

    @GetMapping("/{id}")
    public Result<PostDetailVO> getPostById(@PathVariable Long id) {
        Long userId = getOptionalUserId();
        return Result.success(postService.getPostById(id, userId));
    }

    @PostMapping
    public Result<PostDetailVO> createPost(@Valid @RequestBody PostCreateDTO dto) {
        return Result.success(postService.createPost(getCurrentUserId(), dto));
    }

    @PutMapping("/{id}")
    public Result<PostDetailVO> updatePost(@PathVariable Long id, @Valid @RequestBody PostUpdateDTO dto) {
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
        postService.react(id, getCurrentUserId(), reactionType);
        return Result.success();
    }

    @PostMapping("/{id}/favorite")
    public Result<Boolean> toggleFavorite(@PathVariable Long id) {
        return Result.success(postService.toggleFavorite(id, getCurrentUserId()));
    }

    @GetMapping("/favorites")
    public Result<List<PostDetailVO>> getFavorites() {
        return Result.success(postService.getFavoritePosts(getCurrentUserId()));
    }
}
