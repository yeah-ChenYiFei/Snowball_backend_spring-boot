package com.snowball.controller;

import com.snowball.common.BusinessException;
import com.snowball.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import com.snowball.dto.CommentCreateDTO;
import com.snowball.service.CommentService;
import com.snowball.service.IdempotencyService;
import com.snowball.service.RateLimitService;
import com.snowball.vo.CommentVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
public class CommentController extends BaseController {

    private final CommentService commentService;
    private final RateLimitService rateLimitService;
    private final IdempotencyService idempotencyService;
    private final HttpServletRequest httpRequest;

    public CommentController(CommentService commentService, RateLimitService rateLimitService,
                             IdempotencyService idempotencyService, HttpServletRequest httpRequest) {
        this.commentService = commentService;
        this.rateLimitService = rateLimitService;
        this.idempotencyService = idempotencyService;
        this.httpRequest = httpRequest;
    }

    @GetMapping
    public Result<List<CommentVO>> getComments(@PathVariable Long postId) {
        Long userId = getOptionalUserId();
        return Result.success(commentService.getCommentsByPostId(postId, userId));
    }

    @PostMapping
    public Result<Void> createComment(@PathVariable Long postId, @Valid @RequestBody CommentCreateDTO dto) {
        Long userId = getCurrentUserId();
        // Rate limit check
        var rateResult = rateLimitService.checkCommentLimit(getClientIp());
        if (!rateResult.allowed()) {
            throw new BusinessException(429, "操作太频繁，请" + rateResult.retryAfterSeconds() + "秒后再试");
        }
        // Idempotency check
        String idemToken = httpRequest.getHeader("X-Idempotency-Key");
        if (!idempotencyService.checkAndConsume(userId, "comment:create", idemToken)) {
            throw new BusinessException(409, "请勿重复提交");
        }
        commentService.createComment(postId, userId, dto);
        return Result.success();
    }

    @PostMapping("/{commentId}/react")
    public Result<String> reactToComment(@PathVariable Long postId, @PathVariable Long commentId,
                                         @RequestParam String type) {
        commentService.reactToComment(commentId, getCurrentUserId(), type);
        return Result.success("ok");
    }

    private String getClientIp() {
        String forwarded = httpRequest.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return httpRequest.getRemoteAddr();
    }
}
