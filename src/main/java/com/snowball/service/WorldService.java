package com.snowball.service;

import com.snowball.dto.WorldCreateDTO;
import com.snowball.dto.WorldUpdateDTO;
import com.snowball.vo.JoinRequestVO;
import com.snowball.vo.WorldVO;

import java.util.List;

public interface WorldService {
    List<WorldVO> getAccessibleWorlds(Long userId);
    List<WorldVO> getPublicWorlds();
    WorldVO getWorldById(Long id, Long userId);
    WorldVO createWorld(Long userId, WorldCreateDTO dto);
    WorldVO updateWorld(Long worldId, Long userId, WorldUpdateDTO dto);
    void deleteWorld(Long worldId, Long userId);
    void checkWorldAccess(Long worldId, Long userId);

    JoinRequestVO requestJoin(Long worldId, Long applicantId, String reason);
    List<JoinRequestVO> getJoinRequests(Long worldId, Long ownerId);
    void handleJoinRequest(Long requestId, Long ownerId, boolean approved);
}
