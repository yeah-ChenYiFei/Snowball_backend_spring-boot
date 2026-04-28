package com.example.snowball.controller;

import com.example.snowball.common.Result;
import com.example.snowball.entity.Comment;
import com.example.snowball.repository.CommentRepository;
import com.example.snowball.repository.UserRepository; // ✅ 引入 UserRepository
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
public class CommentController {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository; // ✅ 声明 UserRepository

    // ✅ 构造函数里加上 UserRepository
    public CommentController(CommentRepository commentRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    // ✅ 重点修改这里：不再直接返回 Comment 实体，而是手动拼装带 authorName 的 Map
    @GetMapping
    public Result<List<Map<String, Object>>> getComments(@PathVariable Long postId) {
        List<Comment> comments = commentRepository.findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(postId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Comment c : comments) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("body", c.getBody());
            map.put("parentId", c.getParentId());
            map.put("userId", c.getUserId());
            map.put("createdAt", c.getCreatedAt());

            // ✅ 这就是你要找的逻辑：根据 userId 查出名字，并且 key 叫 "authorName"
            userRepository.findById(c.getUserId()).ifPresent(user -> {
                map.put("authorName", user.getUsername());
            });

            result.add(map);
        }
        return Result.success(result);
    }

    @PostMapping
    public Result<Comment> createComment(@PathVariable Long postId, @RequestBody Map<String, String> body, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setBody(body.get("body"));

        String parentIdStr = body.get("parentId");
        if (parentIdStr != null && !parentIdStr.isEmpty()) {
            comment.setParentId(Long.parseLong(parentIdStr));
        }

        return Result.success(commentRepository.save(comment));
    }
}
