package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.dto.WorldCreateDTO;
import com.snowball.dto.WorldUpdateDTO;
import com.snowball.entity.World;
import com.snowball.entity.WorldCollaborator;
import com.snowball.repository.WorldCollaboratorRepository;
import com.snowball.repository.WorldEntryRepository;
import com.snowball.repository.WorldRelationRepository;
import com.snowball.repository.WorldRepository;
import com.snowball.service.WorldService;
import com.snowball.vo.CollaboratorVO;
import com.snowball.vo.WorldVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorldServiceImpl implements WorldService {

    private final WorldRepository worldRepository;
    private final WorldEntryRepository entryRepository;
    private final WorldRelationRepository relationRepository;
    private final WorldCollaboratorRepository collaboratorRepository;

    public WorldServiceImpl(WorldRepository worldRepository,
                            WorldEntryRepository entryRepository,
                            WorldRelationRepository relationRepository,
                            WorldCollaboratorRepository collaboratorRepository) {
        this.worldRepository = worldRepository;
        this.entryRepository = entryRepository;
        this.relationRepository = relationRepository;
        this.collaboratorRepository = collaboratorRepository;
    }

    @Override
    public List<WorldVO> getAccessibleWorlds(Long userId) {
        List<World> worlds = new ArrayList<>();

        // 自己的世界（全部可见）
        worlds.addAll(worldRepository.findByUserIdOrderByCreatedAtDesc(userId));
        // 别人公开的世界
        worlds.addAll(worldRepository.findByIsPublicTrueAndUserIdNotOrderByCreatedAtDesc(userId));
        // 共创的世界
        worlds.addAll(worldRepository.findByCollaboratorUserId(userId));

        return worlds.stream().map(w -> toVO(w, userId)).collect(Collectors.toList());
    }

    @Override
    public WorldVO getWorldById(Long id, Long userId) {
        World world = worldRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "世界不存在"));

        // 私有世界只有创建者或共创者能看
        if (Boolean.FALSE.equals(world.getIsPublic()) && !world.getUserId().equals(userId)
                && !collaboratorRepository.existsByWorldIdAndUserId(id, userId)) {
            throw new BusinessException(403, "这个世界是私有的，只有创建者可以查看");
        }
        return toVO(world, userId);
    }

    @Override
    public void checkWorldAccess(Long worldId, Long userId) {
        World world = worldRepository.findById(worldId)
                .orElseThrow(() -> new BusinessException(404, "世界不存在"));
        if (Boolean.FALSE.equals(world.getIsPublic()) && !world.getUserId().equals(userId)
                && !collaboratorRepository.existsByWorldIdAndUserId(worldId, userId)) {
            throw new BusinessException(403, "这个世界是私有的，只有创建者可以查看");
        }
    }

    @Override
    public WorldVO updateWorld(Long worldId, Long userId, WorldUpdateDTO dto) {
        World world = worldRepository.findById(worldId)
                .orElseThrow(() -> new BusinessException(404, "世界不存在"));
        if (!world.getUserId().equals(userId)) {
            throw new BusinessException(403, "只有创建者才能编辑");
        }
        if (dto.getName() != null) world.setName(dto.getName());
        if (dto.getDescription() != null) world.setDescription(dto.getDescription());
        if (dto.getType() != null) world.setType(dto.getType());
        if (dto.getIsPublic() != null) world.setIsPublic(dto.getIsPublic());
        return toVO(worldRepository.save(world), userId);
    }

    @Override
    @Transactional
    public void deleteWorld(Long worldId, Long userId) {
        World world = worldRepository.findById(worldId)
                .orElseThrow(() -> new BusinessException(404, "世界不存在"));
        if (!world.getUserId().equals(userId)) {
            throw new BusinessException(403, "只有创建者才能删除");
        }
        relationRepository.deleteAll(relationRepository.findByWorldIdOrderByCreatedAtDesc(worldId));
        entryRepository.deleteAll(entryRepository.findByWorldIdOrderByCreatedAtDesc(worldId));
        worldRepository.delete(world);
    }

    @Override
    public WorldVO createWorld(Long userId, WorldCreateDTO dto) {
        World world = new World();
        world.setUserId(userId);
        world.setName(dto.getName());
        world.setDescription(dto.getDescription());
        world.setType(dto.getType());
        world.setIsPublic(dto.getIsPublic());
        return toVO(worldRepository.save(world), userId);
    }

    private WorldVO toVO(World w) {
        return toVO(w, null);
    }

    private WorldVO toVO(World w, Long currentUserId) {
        WorldVO vo = new WorldVO();
        vo.setId(w.getId());
        vo.setUserId(w.getUserId());
        vo.setName(w.getName());
        vo.setDescription(w.getDescription());
        vo.setType(w.getType());
        vo.setIsPublic(w.getIsPublic());
        vo.setCreatedAt(w.getCreatedAt());
        vo.setUpdatedAt(w.getUpdatedAt());

        if (currentUserId != null) {
            vo.setIsOwner(w.getUserId().equals(currentUserId));
            vo.setIsCollaborator(collaboratorRepository.existsByWorldIdAndUserId(w.getId(), currentUserId));

            List<WorldCollaborator> collabs = collaboratorRepository.findByWorldId(w.getId());
            if (!collabs.isEmpty()) {
                vo.setCollaborators(collabs.stream().map(c -> {
                    CollaboratorVO cv = new CollaboratorVO();
                    cv.setUserId(c.getUserId());
                    cv.setRole(c.getRole());
                    cv.setSince(c.getCreatedAt());
                    return cv;
                }).toList());
            }
        }
        return vo;
    }
}
