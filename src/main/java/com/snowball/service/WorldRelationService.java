package com.snowball.service;

import com.snowball.dto.WorldRelationCreateDTO;
import com.snowball.vo.WorldRelationVO;

import java.util.List;

public interface WorldRelationService {
    List<WorldRelationVO> getRelations(Long worldId);
    WorldRelationVO getRelation(Long relationId);
    WorldRelationVO createRelation(Long worldId, Long userId, WorldRelationCreateDTO dto);
    WorldRelationVO updateRelation(Long relationId, Long userId, WorldRelationCreateDTO dto);
    void deleteRelation(Long relationId, Long userId);
}
