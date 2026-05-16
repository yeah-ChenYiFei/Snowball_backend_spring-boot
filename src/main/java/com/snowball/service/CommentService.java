package com.snowball.service;

import com.snowball.dto.CommentCreateDTO;
import com.snowball.vo.CommentVO;
import java.util.List;

public interface CommentService {
    List<CommentVO> getCommentsByPostId(Long postId);
    void createComment(Long postId, Long userId, CommentCreateDTO dto);
}
