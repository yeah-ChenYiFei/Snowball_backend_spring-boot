package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.dto.WorldCreateDTO;
import com.snowball.dto.WorldUpdateDTO;
import com.snowball.entity.JoinRequest;
import com.snowball.entity.World;
import com.snowball.entity.WorldCollaborator;
import com.snowball.repository.*;
import com.snowball.service.WorldService;
import com.snowball.vo.CollaboratorVO;
import com.snowball.vo.JoinRequestVO;
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
    private final JoinRequestRepository joinRequestRepository;
    private final UserRepository userRepository;

    public WorldServiceImpl(WorldRepository worldRepository,
                            WorldEntryRepository entryRepository,
                            WorldRelationRepository relationRepository,
                            WorldCollaboratorRepository collaboratorRepository,
                            JoinRequestRepository joinRequestRepository,
                            UserRepository userRepository) {
        this.worldRepository = worldRepository;
        this.entryRepository = entryRepository;
        this.relationRepository = relationRepository;
        this.collaboratorRepository = collaboratorRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<WorldVO> getAccessibleWorlds(Long userId) {
        List<World> worlds = new ArrayList<>();
        worlds.addAll(worldRepository.findByUserIdOrderByCreatedAtDesc(userId));
        worlds.addAll(worldRepository.findByIsPublicTrueAndUserIdNotOrderByCreatedAtDesc(userId));
        worlds.addAll(worldRepository.findByCollaboratorUserId(userId));
        return worlds.stream().map(w -> toVO(w, userId)).collect(Collectors.toList());
    }

    @Override
    public List<WorldVO> getPublicWorlds() {
        return worldRepository.findByIsPublicTrue().stream()
                .map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public WorldVO getWorldById(Long id, Long userId) {
        World world = worldRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "世界不存在"));
        if (Boolean.FALSE.equals(world.getIsPublic()) && !world.getUserId().equals(userId)
                && (userId == null || !collaboratorRepository.existsByWorldIdAndUserId(id, userId))) {
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
    @Transactional
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
    @Transactional
    public WorldVO createWorld(Long userId, WorldCreateDTO dto) {
        World world = new World();
        world.setUserId(userId);
        world.setName(dto.getName());
        world.setDescription(dto.getDescription());
        world.setType(dto.getType());
        world.setIsPublic(dto.getIsPublic());
        return toVO(worldRepository.save(world), userId);
    }

    // ===== Join requests =====

    @Override
    public JoinRequestVO requestJoin(Long worldId, Long applicantId, String reason) {
        World world = worldRepository.findById(worldId)
                .orElseThrow(() -> new BusinessException(404, "世界不存在"));

        if (collaboratorRepository.existsByWorldIdAndUserId(worldId, applicantId)) {
            throw new BusinessException(400, "你已经是该世界的共创者");
        }

        boolean already = joinRequestRepository.existsByWorldIdAndApplicantIdAndStatus(
                worldId, applicantId, JoinRequest.JoinRequestStatus.PENDING);
        if (already) {
            throw new BusinessException(400, "你已经提交过申请，请等待主人审核");
        }

        JoinRequest req = new JoinRequest();
        req.setWorldId(worldId);
        req.setApplicantId(applicantId);
        req.setReason(reason);
        req = joinRequestRepository.save(req);

        JoinRequestVO vo = new JoinRequestVO();
        vo.setId(req.getId());
        vo.setWorldId(req.getWorldId());
        vo.setApplicantId(req.getApplicantId());
        vo.setReason(req.getReason());
        vo.setStatus(req.getStatus().name());
        vo.setCreatedAt(req.getCreatedAt());
        userRepository.findById(applicantId).ifPresent(u -> vo.setApplicantName(u.getUsername()));
        return vo;
    }

    @Override
    public List<JoinRequestVO> getJoinRequests(Long worldId, Long ownerId) {
        World world = worldRepository.findById(worldId)
                .orElseThrow(() -> new BusinessException(404, "世界不存在"));
        if (!world.getUserId().equals(ownerId)) {
            throw new BusinessException(403, "只有创建者才能查看申请");
        }
        return joinRequestRepository.findByWorldIdOrderByCreatedAtDesc(worldId).stream().map(r -> {
            JoinRequestVO vo = new JoinRequestVO();
            vo.setId(r.getId());
            vo.setWorldId(r.getWorldId());
            vo.setApplicantId(r.getApplicantId());
            vo.setReason(r.getReason());
            vo.setStatus(r.getStatus().name());
            vo.setCreatedAt(r.getCreatedAt());
            userRepository.findById(r.getApplicantId()).ifPresent(u -> vo.setApplicantName(u.getUsername()));
            return vo;
        }).toList();
    }

    @Override
    @Transactional
    public void handleJoinRequest(Long requestId, Long ownerId, boolean approved) {
        JoinRequest req = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(404, "申请不存在"));

        World world = worldRepository.findById(req.getWorldId())
                .orElseThrow(() -> new BusinessException(404, "世界不存在"));

        if (!world.getUserId().equals(ownerId)) {
            throw new BusinessException(403, "只有创建者才能处理申请");
        }

        req.setStatus(approved ? JoinRequest.JoinRequestStatus.APPROVED : JoinRequest.JoinRequestStatus.REJECTED);
        joinRequestRepository.save(req);

        if (approved) {
            WorldCollaborator collab = new WorldCollaborator();
            collab.setWorldId(world.getId());
            collab.setUserId(req.getApplicantId());
            collab.setRole("COLLABORATOR");
            collaboratorRepository.save(collab);
        }
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
        vo.setEntryCount((int) entryRepository.countByWorldId(w.getId()));

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
