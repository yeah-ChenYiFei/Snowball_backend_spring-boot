package com.example.snowball.controller;

import com.example.snowball.common.Result;
import com.example.snowball.dto.CommentCreateDTO;
import com.example.snowball.service.CommentService;
import com.example.snowball.vo.CommentVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
public class CommentController extends BaseController { // ✅ 1. 继承基类

    private final CommentService commentService; // ✅ 2. 只找 Service，不找 Repository

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // ✅ 3. 查询接口：再也不用写 Map 和 for 循环了！
    @GetMapping
    public Result<List<CommentVO>> getComments(@PathVariable Long postId) {
        return Result.success(commentService.getCommentsByPostId(postId));
    }

    // ✅ 4. 保存接口：不用传 Authentication 了，直接调基类方法
    @PostMapping
    public Result<Void> createComment(@PathVariable Long postId, @RequestBody CommentCreateDTO dto) {
        commentService.createComment(postId, getCurrentUserId(), dto);
        return Result.success();
    }
}
