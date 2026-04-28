package com.example.snowball.controller;

import com.example.snowball.common.Result;
import com.example.snowball.dto.PostCreateDTO;
import com.example.snowball.dto.PostUpdateDTO;
import com.example.snowball.entity.PostVersion;
import com.example.snowball.service.PostService;
import com.example.snowball.vo.PostDetailVO;
import org.springframework.security.access.prepost.PreAuthorize; // ✅ 引入权限注解
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    // ✅ 修复构造器：去掉了乱七八糟的参数，只留真正需要的
    public PostController(PostService postService) {
        this.postService = postService;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户未登录");
        }
        return (Long) authentication.getPrincipal();
    }

    @GetMapping("/ping")
    public Map<String, Object> ping() { return Map.of("time", java.time.LocalDateTime.now().toString(), "message", "pong"); }

    @GetMapping
    public Result<List<PostDetailVO>> getAllPosts() { return Result.success(postService.getAllPosts()); }

    @GetMapping("/{id}")
    public Result<PostDetailVO> getPostById(@PathVariable Long id) { return Result.success(postService.getPostById(id)); }

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
    public Result<List<PostVersion>> getVersions(@PathVariable Long id) { return Result.success(postService.getPostVersions(id)); }

    @PostMapping("/{id}/versions/{verId}/rollback")
    public Result<PostDetailVO> rollbackPost(@PathVariable Long id, @PathVariable Long verId) {
        return Result.success(postService.rollbackPost(id, verId, getCurrentUserId()));
    }

    // ✅ 核心重构：这才是规范的管理员接口写法！
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('SYS_ADMIN')") // 拦截器发现不是 SYS_ADMIN，直接返回 403，根本进不来这个方法！
    public Result<Void> adminDeletePost(@PathVariable Long id) {
        // 这里多么干净！再也不需要查用户、不需要 if 判断了
        postService.forceDeletePost(id);
        return Result.success();
    }
}
