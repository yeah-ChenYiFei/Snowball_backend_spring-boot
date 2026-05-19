package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.dto.CommentCreateDTO;
import com.snowball.service.CommentService;
import com.snowball.vo.CommentVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
public class GenericCommentController extends BaseController {

    private final CommentService commentService;

    public GenericCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public Result<List<CommentVO>> getComments(@RequestParam String sourceType, @RequestParam Long sourceId) {
        return Result.success(commentService.getCommentsBySource(sourceType, sourceId, getOptionalUserId()));
    }

    @PostMapping
    public Result<Void> createComment(@RequestBody CommentCreateDTO dto,
                                      @RequestParam String sourceType,
                                      @RequestParam Long sourceId) {
        commentService.createGenericComment(sourceType, sourceId, getCurrentUserId(), dto);
        return Result.success();
    }

    @PostMapping("/{commentId}/react")
    public Result<String> reactToComment(@PathVariable Long commentId, @RequestParam String type) {
        commentService.reactToComment(commentId, getCurrentUserId(), type);
        return Result.success("ok");
    }
}
