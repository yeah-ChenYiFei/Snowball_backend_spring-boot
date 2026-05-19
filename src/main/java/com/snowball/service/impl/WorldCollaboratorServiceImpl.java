package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.entity.Friendship;
import com.snowball.entity.User;
import com.snowball.entity.World;
import com.snowball.entity.WorldCollaborator;
import com.snowball.repository.FriendshipRepository;
import com.snowball.repository.UserRepository;
import com.snowball.repository.WorldCollaboratorRepository;
import com.snowball.repository.WorldRepository;
import com.snowball.service.NotificationService;
import com.snowball.service.WorldCollaboratorService;
import com.snowball.vo.CollaboratorVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WorldCollaboratorServiceImpl implements WorldCollaboratorService {

    private final WorldCollaboratorRepository collaboratorRepository;
    private final WorldRepository worldRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public WorldCollaboratorServiceImpl(WorldCollaboratorRepository collaboratorRepository,
                                         WorldRepository worldRepository,
                                         FriendshipRepository friendshipRepository,
                                         UserRepository userRepository,
                                         NotificationService notificationService) {
        this.collaboratorRepository = collaboratorRepository;
        this.worldRepository = worldRepository;
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    public List<CollaboratorVO> getCollaborators(Long worldId) {
        List<WorldCollaborator> list = collaboratorRepository.findByWorldId(worldId);
        if (list.isEmpty()) return List.of();

        List<Long> userIds = list.stream().map(WorldCollaborator::getUserId).distinct().toList();
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return list.stream().map(c -> {
            CollaboratorVO vo = new CollaboratorVO();
            vo.setUserId(c.getUserId());
            User u = userMap.get(c.getUserId());
            vo.setUsername(u != null ? u.getUsername() : "");
            vo.setAvatarUrl(u != null ? u.getAvatarUrl() : null);
            vo.setRole(c.getRole());
            vo.setSince(c.getCreatedAt());
            return vo;
        }).toList();
    }

    @Override
    @Transactional
    public CollaboratorVO addCollaborator(Long worldId, Long ownerId, Long friendId) {
        World world = worldRepository.findById(worldId)
                .orElseThrow(() -> new BusinessException(404, "世界不存在"));
        if (!world.getUserId().equals(ownerId)) {
            throw new BusinessException(403, "只有世界主人可以添加共创者");
        }
        if (ownerId.equals(friendId)) {
            throw new BusinessException(400, "不能添加自己为共创者");
        }

        // Verify they are friends
        friendshipRepository.findAcceptedFriendship(ownerId, friendId)
                .orElseThrow(() -> new BusinessException(400, "只能添加好友为共创者"));

        if (collaboratorRepository.existsByWorldIdAndUserId(worldId, friendId)) {
            throw new BusinessException(400, "该用户已是共创者");
        }

        WorldCollaborator c = new WorldCollaborator();
        c.setWorldId(worldId);
        c.setUserId(friendId);
        c.setRole("COLLABORATOR");
        c = collaboratorRepository.save(c);

        String ownerName = userRepository.findById(ownerId).map(User::getUsername).orElse("");
        notificationService.create(friendId, "WORLD_COLLABORATOR_ADDED", worldId, "WORLD",
                ownerId, ownerName + "将您添加为世界「" + world.getName() + "」的共创者");

        CollaboratorVO vo = new CollaboratorVO();
        vo.setUserId(friendId);
        userRepository.findById(friendId).ifPresent(u -> vo.setUsername(u.getUsername()));
        vo.setRole("COLLABORATOR");
        vo.setSince(c.getCreatedAt());
        return vo;
    }

    @Override
    @Transactional
    public void removeCollaborator(Long worldId, Long ownerId, Long userId) {
        World world = worldRepository.findById(worldId)
                .orElseThrow(() -> new BusinessException(404, "世界不存在"));
        if (!world.getUserId().equals(ownerId)) {
            throw new BusinessException(403, "只有世界主人可以移除共创者");
        }
        collaboratorRepository.deleteByWorldIdAndUserId(worldId, userId);
    }
}
