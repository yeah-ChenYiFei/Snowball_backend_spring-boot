package com.snowball.service;

import com.snowball.dto.ChainCreateDTO;
import com.snowball.dto.ChainSegmentCreateDTO;
import com.snowball.vo.ChainDetailVO;
import com.snowball.vo.ChainSegmentVO;
import com.snowball.vo.ChainVO;
import java.util.List;

public interface ChainService {
    List<ChainVO> getAllChains();
    ChainDetailVO getChainDetail(Long chainId);
    ChainVO createChain(Long userId, ChainCreateDTO dto);
    ChainSegmentVO addSegment(Long chainId, Long userId, ChainSegmentCreateDTO dto);
}