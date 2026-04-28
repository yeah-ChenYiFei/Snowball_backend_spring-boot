package com.example.snowball.controller;

import com.example.snowball.common.Result;
import com.example.snowball.dto.PostCreateDTO;
import com.example.snowball.dto.PostUpdateDTO;
import com.example.snowball.entity.PostVersion;
import com.example.snowball.service.PostService;
import com.example.snowball.vo.PostDetailVO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // ✅ 新增：工具方法，从当前 JWT Token 中提取真实用户 ID
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户未登录");
        }
        // 前提：你的 JwtAuthenticationFilter 在设置 Authentication 时，Principal 存的是用户的 ID (Long类型)
        return (Long) authentication.getPrincipal();
    }

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of("time", java.time.LocalDateTime.now().toString(), "message", "pong");
    }

    // 1. 获取广场列表
    @GetMapping
    public Result<List<PostDetailVO>> getAllPosts() {
        return Result.success(postService.getAllPosts());
    }

    // 2. 获取单个详情
    @GetMapping("/{id}")
    public Result<PostDetailVO> getPostById(@PathVariable Long id) {
        return Result.success(postService.getPostById(id));
    }

    // 3. 创建帖子
    @PostMapping
    public Result<PostDetailVO> createPost(@RequestBody PostCreateDTO dto) {
        // ✅ 修复：替换掉 mockUserId = 1L
        Long currentUserId = getCurrentUserId();
        return Result.success(postService.createPost(currentUserId, dto));
    }

    // 4. 编辑帖子（触发版本控制算法）
    @PutMapping("/{id}")
    public Result<PostDetailVO> updatePost(@PathVariable Long id, @RequestBody PostUpdateDTO dto) {
        // ✅ 修复
        Long currentUserId = getCurrentUserId();
        return Result.success(postService.updatePost(id, currentUserId, dto));
    }

    // 5. 逻辑删除帖子
    @DeleteMapping("/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        // ✅ 修复：现在如果 B 删 A 的帖子，Service 层会发现 currentUserId != post.getUserId()，抛出 403 异常
        Long currentUserId = getCurrentUserId();
        postService.deletePost(id, currentUserId);
        return Result.success();
    }

    // 6. 获取版本历史列表
    @GetMapping("/{id}/versions")
    public Result<List<PostVersion>> getVersions(@PathVariable Long id) {
        return Result.success(postService.getPostVersions(id));
    }

    // 7. 回滚至指定版本
    @PostMapping("/{id}/versions/{verId}/rollback")
    public Result<PostDetailVO> rollbackPost(@PathVariable Long id, @PathVariable Long verId) {
        // ✅ 修复
        Long currentUserId = getCurrentUserId();
        return Result.success(postService.rollbackPost(id, verId, currentUserId));
    }
}
