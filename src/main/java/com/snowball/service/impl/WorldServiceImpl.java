package com.snowball.service.impl;

import com.snowball.dto.WorldCreateDTO;
import com.snowball.entity.World;
import com.snowball.repository.WorldRepository;
import com.snowball.service.WorldService;
import com.snowball.vo.WorldVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorldServiceImpl implements WorldService {

    private final WorldRepository worldRepository;

    public WorldServiceImpl(WorldRepository worldRepository) {
        this.worldRepository = worldRepository;
    }

    @Override
    public List<WorldVO> getMyWorlds(Long userId) {
        return worldRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public WorldVO getWorldById(Long id) {
        World world = worldRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("世界不存在"));
        return toVO(world);
    }

    @Override
    public WorldVO createWorld(Long userId, WorldCreateDTO dto) {
        World world = new World();
        world.setUserId(userId);
        world.setName(dto.getName());
        world.setDescription(dto.getDescription());
        world.setType(dto.getType());
        return toVO(worldRepository.save(world));
    }

    private WorldVO toVO(World w) {
        WorldVO vo = new WorldVO();
        vo.setId(w.getId());
        vo.setUserId(w.getUserId());
        vo.setName(w.getName());
        vo.setDescription(w.getDescription());
        vo.setType(w.getType());
        vo.setCreatedAt(w.getCreatedAt());
        vo.setUpdatedAt(w.getUpdatedAt());
        return vo;
    }
}
