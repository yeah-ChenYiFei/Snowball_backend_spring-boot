package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.dto.WorldCreateDTO;
import com.snowball.dto.WorldUpdateDTO;
import com.snowball.entity.World;
import com.snowball.repository.WorldEntryRepository;
import com.snowball.repository.WorldRelationRepository;
import com.snowball.repository.WorldRepository;
import com.snowball.service.WorldService;
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

    public WorldServiceImpl(WorldRepository worldRepository,
                            WorldEntryRepository entryRepository,
                            WorldRelationRepository relationRepository) {
        this.worldRepository = worldRepository;
        this.entryRepository = entryRepository;
        this.relationRepository = relationRepository;
    }

    @Override
    public List<WorldVO> getAccessibleWorlds(Long userId) {
        List<World> worlds = new ArrayList<>();

        // 自己的世界（全部可见）
        worlds.addAll(worldRepository.findByUserIdOrderByCreatedAtDesc(userId));
        // 别人公开的世界
        worlds.addAll(worldRepository.findByIsPublicTrueAndUserIdNotOrderByCreatedAtDesc(userId));

        return worlds.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public WorldVO getWorldById(Long id, Long userId) {
        World world = worldRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "世界不存在"));

        // 私有世界只有创建者能看
        if (Boolean.FALSE.equals(world.getIsPublic()) && !world.getUserId().equals(userId)) {
            throw new BusinessException(403, "这个世界是私有的，只有创建者可以查看");
        }
        return toVO(world);
    }

    @Override
    public void checkWorldAccess(Long worldId, Long userId) {
        World world = worldRepository.findById(worldId)
                .orElseThrow(() -> new BusinessException(404, "世界不存在"));
        if (Boolean.FALSE.equals(world.getIsPublic()) && !world.getUserId().equals(userId)) {
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
        return toVO(worldRepository.save(world));
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
        return toVO(worldRepository.save(world));
    }

    private WorldVO toVO(World w) {
        WorldVO vo = new WorldVO();
        vo.setId(w.getId());
        vo.setUserId(w.getUserId());
        vo.setName(w.getName());
        vo.setDescription(w.getDescription());
        vo.setType(w.getType());
        vo.setIsPublic(w.getIsPublic());
        vo.setCreatedAt(w.getCreatedAt());
        vo.setUpdatedAt(w.getUpdatedAt());
        return vo;
    }
}
