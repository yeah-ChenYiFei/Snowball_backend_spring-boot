package com.snowball.service;

import com.snowball.dto.ChainCreateDTO;
import com.snowball.dto.ChainSegmentCreateDTO;
import com.snowball.dto.SegmentCommentCreateDTO;
import com.snowball.vo.*;
import java.util.List;

public interface ChainService {
    List<ChainVO> getAllChains();
    List<ChainVO> getPublicChains();
    List<ChainVO> getGroupChains(Long groupId);
    ChainDetailVO getChainDetail(Long chainId);
    ChainVO createChain(Long userId, ChainCreateDTO dto);
    ChainSegmentVO addSegment(Long chainId, Long userId, ChainSegmentCreateDTO dto);
    void deleteSegment(Long segmentId, Long userId);

    List<SegmentCommentVO> getComments(Long segmentId);
    SegmentCommentVO addComment(Long segmentId, Long userId, SegmentCommentCreateDTO dto);
    void reviewSegment(Long segmentId, Long reviewerUserId, String status);

    List<ChainVO> getUserChainActivities(Long userId);
}
