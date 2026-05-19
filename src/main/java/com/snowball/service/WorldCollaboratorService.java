package com.snowball.service;

import com.snowball.vo.CollaboratorVO;

import java.util.List;

public interface WorldCollaboratorService {
    List<CollaboratorVO> getCollaborators(Long worldId);
    CollaboratorVO addCollaborator(Long worldId, Long ownerId, Long friendId);
    void removeCollaborator(Long worldId, Long ownerId, Long userId);
}
