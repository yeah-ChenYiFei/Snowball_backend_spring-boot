package com.snowball.service;

import com.snowball.dto.WorldCreateDTO;
import com.snowball.vo.WorldVO;

import java.util.List;

public interface WorldService {
    List<WorldVO> getMyWorlds(Long userId);
    WorldVO getWorldById(Long id);
    WorldVO createWorld(Long userId, WorldCreateDTO dto);
}
