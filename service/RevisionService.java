package com.example.snowball.service;
import com.example.snowball.dto.RevisionCreateDTO;
import com.example.snowball.vo.RevisionVO;
import java.util.List;
public interface RevisionService {
    List<RevisionVO> getRevisions(Long postId);
    RevisionVO createRevision(Long postId, Long userId, RevisionCreateDTO dto);
}