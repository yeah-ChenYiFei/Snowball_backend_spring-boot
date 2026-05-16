package com.snowball.service;
import com.snowball.dto.RevisionCreateDTO;
import com.snowball.vo.RevisionVO;
import java.util.List;
public interface RevisionService {
    List<RevisionVO> getRevisions(Long postId);
    RevisionVO createRevision(Long postId, Long userId, RevisionCreateDTO dto);
}