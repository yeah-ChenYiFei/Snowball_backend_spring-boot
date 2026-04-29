package com.example.snowball.service;

import com.example.snowball.dto.ChainCreateDTO;
import com.example.snowball.dto.ChainSegmentCreateDTO;
import com.example.snowball.vo.ChainSegmentVO;
import com.example.snowball.vo.ChainVO;
import java.util.List;

public interface ChainService {
    List<ChainVO> getAllChains();
    List<ChainSegmentVO> getChainDetail(Long chainId);
    ChainVO createChain(Long userId, ChainCreateDTO dto);
    ChainSegmentVO addSegment(Long chainId, Long userId, ChainSegmentCreateDTO dto);
}