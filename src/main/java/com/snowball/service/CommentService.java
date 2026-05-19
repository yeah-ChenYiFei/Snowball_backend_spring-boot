package com.snowball.service;

import com.snowball.dto.CommentCreateDTO;
import com.snowball.vo.CommentVO;

import java.util.List;

public interface CommentService {
    List<CommentVO> getCommentsByPostId(Long postId, Long currentUserId);
    List<CommentVO> getCommentsBySource(String sourceType, Long sourceId, Long currentUserId);
    void createComment(Long postId, Long userId, CommentCreateDTO dto);
    void createGenericComment(String sourceType, Long sourceId, Long userId, CommentCreateDTO dto);
    void reactToComment(Long commentId, Long userId, String reactionType);
}
