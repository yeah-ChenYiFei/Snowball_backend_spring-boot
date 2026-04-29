package com.example.snowball.service.impl;

import com.example.snowball.dto.CommentCreateDTO;
import com.example.snowball.entity.Comment;
import com.example.snowball.repository.CommentRepository;
import com.example.snowball.repository.UserRepository;
import com.example.snowball.service.CommentService;
import com.example.snowball.vo.CommentVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public CommentServiceImpl(CommentRepository commentRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<CommentVO> getCommentsByPostId(Long postId) {
        List<Comment> comments = commentRepository.findByPostIdAndIsDeletedFalseOrderByCreatedAtAsc(postId);
        List<CommentVO> voList = new ArrayList<>();

        for (Comment c : comments) {
            CommentVO vo = new CommentVO();
            vo.setId(c.getId());
            vo.setBody(c.getBody());
            vo.setParentId(c.getParentId());
            vo.setUserId(c.getUserId());
            vo.setCreatedAt(c.getCreatedAt());

            // 核心：在这里查作者名字
            userRepository.findById(c.getUserId()).ifPresent(user -> {
                vo.setAuthorName(user.getUsername());
            });

            voList.add(vo);
        }
        return voList;
    }

    @Override
    public void createComment(Long postId, Long userId, CommentCreateDTO dto) {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setBody(dto.getBody());
        comment.setParentId(dto.getParentId()); // DTO里如果没有就是null，不影响
        commentRepository.save(comment);
    }
}
