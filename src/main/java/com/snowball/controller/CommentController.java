package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.dto.CommentCreateDTO;
import com.snowball.service.CommentService;
import com.snowball.vo.CommentVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
public class CommentController extends BaseController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public Result<List<CommentVO>> getComments(@PathVariable Long postId) {
        Long userId = getOptionalUserId();
        return Result.success(commentService.getCommentsByPostId(postId, userId));
    }

    @PostMapping
    public Result<Void> createComment(@PathVariable Long postId, @RequestBody CommentCreateDTO dto) {
        commentService.createComment(postId, getCurrentUserId(), dto);
        return Result.success();
    }

    @PostMapping("/{commentId}/react")
    public Result<String> reactToComment(@PathVariable Long postId, @PathVariable Long commentId,
                                         @RequestParam String type) {
        commentService.reactToComment(commentId, getCurrentUserId(), type);
        return Result.success("ok");
    }
}
