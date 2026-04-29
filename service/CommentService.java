package com.example.snowball.service;

import com.example.snowball.dto.CommentCreateDTO;
import com.example.snowball.vo.CommentVO;
import java.util.List;

public interface CommentService {
    List<CommentVO> getCommentsByPostId(Long postId);
    void createComment(Long postId, Long userId, CommentCreateDTO dto);
}
