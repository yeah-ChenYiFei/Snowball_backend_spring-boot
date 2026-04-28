package com.example.snowball.controller;
import com.example.snowball.common.Result;
import com.example.snowball.entity.Comment;
import com.example.snowball.repository.CommentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
public class CommentController {
    private final CommentRepository commentRepository;
    public CommentController(CommentRepository commentRepository) { this.commentRepository = commentRepository; }

    @GetMapping
    public Result<List<Comment>> getComments(@PathVariable Long postId) {
        return Result.success(commentRepository.findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(postId));
    }

    @PostMapping
    public Result<Comment> createComment(@PathVariable Long postId, @RequestBody Map<String, String> body, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setBody(body.get("body"));
        if (body.containsKey("parentId") && !body.get("parentId").isEmpty()) {
            comment.setParentId(Long.parseLong(body.get("parentId")));
        }
        return Result.success(commentRepository.save(comment));
    }
}
