package com.snowball.service;

import com.snowball.dto.WorldCreateDTO;
import com.snowball.dto.WorldUpdateDTO;
import com.snowball.vo.WorldVO;

import java.util.List;

public interface WorldService {
    List<WorldVO> getAccessibleWorlds(Long userId);
    WorldVO getWorldById(Long id, Long userId);
    WorldVO createWorld(Long userId, WorldCreateDTO dto);
    WorldVO updateWorld(Long worldId, Long userId, WorldUpdateDTO dto);
    void deleteWorld(Long worldId, Long userId);
    void checkWorldAccess(Long worldId, Long userId);
}
