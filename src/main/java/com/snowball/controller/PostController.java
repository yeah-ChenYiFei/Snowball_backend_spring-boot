package com.snowball.controller;

import com.snowball.common.BusinessException;
import com.snowball.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import com.snowball.dto.PostCreateDTO;
import com.snowball.dto.PostUpdateDTO;
import com.snowball.entity.PostVersion;
import com.snowball.service.IdempotencyService;
import com.snowball.service.PostService;
import com.snowball.service.RateLimitService;
import com.snowball.vo.PostDetailVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController extends BaseController{
    private final PostService postService;
    private final RateLimitService rateLimitService;
    private final IdempotencyService idempotencyService;
    private final HttpServletRequest httpRequest;

    public PostController(PostService postService, RateLimitService rateLimitService,
                          IdempotencyService idempotencyService, HttpServletRequest httpRequest) {
        this.postService = postService;
        this.rateLimitService = rateLimitService;
        this.idempotencyService = idempotencyService;
        this.httpRequest = httpRequest;
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
        Long userId = getCurrentUserId();
        // Rate limit check
        var rateResult = rateLimitService.checkPostLimit(getClientIp());
        if (!rateResult.allowed()) {
            throw new BusinessException(429, "操作太频繁，请" + rateResult.retryAfterSeconds() + "秒后再试");
        }
        // Idempotency check
        String idemToken = httpRequest.getHeader("X-Idempotency-Key");
        if (!idempotencyService.checkAndConsume(userId, "post:create", idemToken)) {
            throw new BusinessException(409, "请勿重复提交");
        }
        return Result.success(postService.createPost(userId, dto));
    }

    /** Simple IP extraction from X-Forwarded-For or remote address */
    private String getClientIp() {
        String forwarded = httpRequest.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return httpRequest.getRemoteAddr();
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

    @PostMapping("/batch-delete")
    public Result<Map<String, Object>> batchDelete(@RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        @SuppressWarnings("unchecked")
        List<Integer> rawIds = (List<Integer>) body.get("ids");
        if (rawIds == null || rawIds.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的帖子");
        }
        List<Long> ids = rawIds.stream().map(Long::valueOf).toList();
        int deleted = postService.batchDelete(ids, userId);
        return Result.success(Map.of("deleted", deleted));
    }

    @PostMapping("/batch-toggle-status")
    public Result<Map<String, Object>> batchToggleStatus(@RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        @SuppressWarnings("unchecked")
        List<Integer> rawIds = (List<Integer>) body.get("ids");
        String newStatus = body.get("status") instanceof String ? (String) body.get("status") : "";
        if (rawIds == null || rawIds.isEmpty()) {
            throw new BusinessException(400, "请选择要操作的帖子");
        }
        if (!"PUBLISHED".equals(newStatus) && !"HIDDEN".equals(newStatus)) {
            throw new BusinessException(400, "状态值无效");
        }
        List<Long> ids = rawIds.stream().map(Long::valueOf).toList();
        int updated = postService.batchToggleStatus(ids, userId, newStatus);
        return Result.success(Map.of("updated", updated, "status", newStatus));
    }

    @PostMapping("/{id}/toggle-status")
    public Result<PostDetailVO> togglePostStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        String status = body.get("status");
        if (status == null || (!"PUBLISHED".equals(status) && !"HIDDEN".equals(status))) {
            throw new BusinessException(400, "状态值无效");
        }
        return Result.success(postService.toggleStatus(id, userId, status));
    }
}
