package com.example.snowball.controller;

import com.example.snowball.common.Result;
import com.example.snowball.dto.PostCreateDTO;
import com.example.snowball.dto.PostUpdateDTO;
import com.example.snowball.entity.PostVersion;
import com.example.snowball.service.PostService;
import com.example.snowball.vo.PostDetailVO;
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
        Long mockUserId = 1L; // 测试阶段写死，JWT搞定后从Token取
        return Result.success(postService.createPost(mockUserId, dto));
    }

    // 4. 编辑帖子（触发版本控制算法）
    @PutMapping("/{id}")
    public Result<PostDetailVO> updatePost(@PathVariable Long id, @RequestBody PostUpdateDTO dto) {
        Long mockUserId = 1L;
        // 正常直接返回，如果发生 403/409 会由 Service 抛出异常，被 GlobalExceptionHandler 拦截
        return Result.success(postService.updatePost(id, mockUserId, dto));
    }

    // 5. 逻辑删除帖子
    @DeleteMapping("/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        Long mockUserId = 1L;
        postService.deletePost(id, mockUserId);
        return Result.success();
    }

    // 6. 获取版本历史列表
    @GetMapping("/{id}/versions")
    public Result<List<PostVersion>> getVersions(@PathVariable Long id) {
        return Result.success(postService.getPostVersions(id));
    }
    // 👇 新增：7. 回滚至指定版本 (对应文档 3.1.2)
    @PostMapping("/{id}/versions/{verId}/rollback")
    public Result<PostDetailVO> rollbackPost(@PathVariable Long id, @PathVariable Long verId) {
        Long mockUserId = 1L;
        return Result.success(postService.rollbackPost(id, verId, mockUserId));
    }
}
