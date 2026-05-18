package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.dto.WorldRelationCreateDTO;
import com.snowball.entity.World;
import com.snowball.entity.WorldEntry;
import com.snowball.entity.WorldRelation;
import com.snowball.repository.WorldEntryRepository;
import com.snowball.repository.WorldRelationRepository;
import com.snowball.repository.WorldRepository;
import com.snowball.service.WorldRelationService;
import com.snowball.vo.WorldRelationVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorldRelationServiceImpl implements WorldRelationService {

    private final WorldRelationRepository relationRepository;
    private final WorldEntryRepository entryRepository;
    private final WorldRepository worldRepository;

    public WorldRelationServiceImpl(WorldRelationRepository relationRepository,
                                    WorldEntryRepository entryRepository,
                                    WorldRepository worldRepository) {
        this.relationRepository = relationRepository;
        this.entryRepository = entryRepository;
        this.worldRepository = worldRepository;
    }

    @Override
    public List<WorldRelationVO> getRelations(Long worldId) {
        return relationRepository.findByWorldIdOrderByCreatedAtDesc(worldId)
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public WorldRelationVO getRelation(Long relationId) {
        WorldRelation rel = relationRepository.findById(relationId)
                .orElseThrow(() -> new RuntimeException("关系不存在"));
        return toVO(rel);
    }

    @Override
    public WorldRelationVO createRelation(Long worldId, Long userId, WorldRelationCreateDTO dto) {
        WorldRelation rel = new WorldRelation();
        rel.setWorldId(worldId);
        rel.setFromEntryId(dto.getFromEntryId());
        rel.setToEntryId(dto.getToEntryId());
        rel.setDirection(WorldRelation.ArrowDirection.valueOf(dto.getDirection()));
        rel.setDescription(dto.getDescription());
        return toVO(relationRepository.save(rel));
    }

    @Override
    public WorldRelationVO updateRelation(Long relationId, Long userId, WorldRelationCreateDTO dto) {
        WorldRelation rel = relationRepository.findById(relationId)
                .orElseThrow(() -> new BusinessException(404, "关系不存在"));
        World world = worldRepository.findById(rel.getWorldId())
                .orElseThrow(() -> new BusinessException(404, "世界不存在"));
        if (!world.getUserId().equals(userId)) {
            throw new BusinessException(403, "只有世界创建者才能修改关系");
        }
        rel.setFromEntryId(dto.getFromEntryId());
        rel.setToEntryId(dto.getToEntryId());
        rel.setDirection(WorldRelation.ArrowDirection.valueOf(dto.getDirection()));
        rel.setDescription(dto.getDescription());
        return toVO(relationRepository.save(rel));
    }

    @Override
    public void deleteRelation(Long relationId, Long userId) {
        WorldRelation rel = relationRepository.findById(relationId)
                .orElseThrow(() -> new BusinessException(404, "关系不存在"));
        World world = worldRepository.findById(rel.getWorldId())
                .orElseThrow(() -> new BusinessException(404, "世界不存在"));
        if (!world.getUserId().equals(userId)) {
            throw new BusinessException(403, "只有世界创建者才能删除关系");
        }
        relationRepository.delete(rel);
    }

    private WorldRelationVO toVO(WorldRelation r) {
        WorldRelationVO vo = new WorldRelationVO();
        vo.setId(r.getId());
        vo.setWorldId(r.getWorldId());
        vo.setFromEntryId(r.getFromEntryId());
        vo.setToEntryId(r.getToEntryId());
        vo.setDirection(r.getDirection().name());
        vo.setDescription(r.getDescription());
        vo.setCreatedAt(r.getCreatedAt());

        entryRepository.findById(r.getFromEntryId()).ifPresent(e -> vo.setFromEntryName(e.getName()));
        entryRepository.findById(r.getToEntryId()).ifPresent(e -> vo.setToEntryName(e.getName()));

        return vo;
    }
}
