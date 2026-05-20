package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.dto.FriendRequestDTO;
import com.snowball.entity.Friendship;
import com.snowball.entity.User;
import com.snowball.repository.FriendshipRepository;
import com.snowball.repository.UserRepository;
import com.snowball.service.FriendService;
import com.snowball.service.NotificationService;
import com.snowball.vo.FriendVO;
import com.snowball.vo.FriendshipStatusVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FriendServiceImpl implements FriendService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public FriendServiceImpl(FriendshipRepository friendshipRepository,
                             UserRepository userRepository,
                             NotificationService notificationService) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    public List<FriendVO> getFriends(Long userId) {
        List<Friendship> list = friendshipRepository.findAllFriends(userId);
        if (list.isEmpty()) return List.of();

        List<Long> friendIds = list.stream()
                .map(f -> f.getUserId().equals(userId) ? f.getFriendId() : f.getUserId())
                .distinct().toList();
        Map<Long, User> userMap = userRepository.findAllById(friendIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return list.stream().map(f -> {
            FriendVO vo = new FriendVO();
            Long fid = f.getUserId().equals(userId) ? f.getFriendId() : f.getUserId();
            User u = userMap.get(fid);
            vo.setUserId(fid);
            vo.setUsername(u != null ? u.getUsername() : "");
            vo.setAvatarUrl(u != null ? u.getAvatarUrl() : null);
            vo.setSince(f.getUpdatedAt() != null ? f.getUpdatedAt() : f.getCreatedAt());
            return vo;
        }).toList();
    }

    @Override
    public List<FriendVO> getPendingRequests(Long userId) {
        List<Friendship> list = friendshipRepository.findByFriendIdAndStatus(userId, Friendship.FriendshipStatus.PENDING);
        if (list.isEmpty()) return List.of();

        List<Long> requesterIds = list.stream().map(Friendship::getUserId).distinct().toList();
        Map<Long, User> userMap = userRepository.findAllById(requesterIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return list.stream().map(f -> {
            FriendVO vo = new FriendVO();
            vo.setUserId(f.getUserId());
            User u = userMap.get(f.getUserId());
            vo.setUsername(u != null ? u.getUsername() : "");
            vo.setAvatarUrl(u != null ? u.getAvatarUrl() : null);
            vo.setSince(f.getCreatedAt());
            return vo;
        }).toList();
    }

    @Override
    public FriendshipStatusVO getStatus(Long userId, Long targetUserId) {
        FriendshipStatusVO vo = new FriendshipStatusVO();
        if (userId.equals(targetUserId)) {
            vo.setStatus("SELF");
            return vo;
        }

        Optional<Friendship> accepted = friendshipRepository.findAcceptedFriendship(userId, targetUserId);
        if (accepted.isPresent()) {
            vo.setStatus("FRIEND");
            return vo;
        }

        Optional<Friendship> any = friendshipRepository.findAnyFriendship(userId, targetUserId);
        if (any.isPresent()) {
            Friendship f = any.get();
            if (f.getStatus() == Friendship.FriendshipStatus.PENDING) {
                if (f.getUserId().equals(userId)) {
                    vo.setStatus("PENDING_TO_THEM");
                } else {
                    vo.setStatus("PENDING_FROM_THEM");
                    vo.setFriendshipId(f.getId());
                }
                return vo;
            }
        }

        vo.setStatus("NONE");
        return vo;
    }

    @Override
    @Transactional
    public void sendRequest(Long userId, FriendRequestDTO dto) {
        if (userId.equals(dto.getFriendId())) {
            throw new BusinessException(400, "不能添加自己为好友");
        }

        Optional<Friendship> existing = friendshipRepository.findAnyFriendship(userId, dto.getFriendId());
        if (existing.isPresent()) {
            Friendship f = existing.get();
            if (f.getStatus() == Friendship.FriendshipStatus.ACCEPTED) {
                throw new BusinessException(400, "已经是好友了");
            }
            if (f.getStatus() == Friendship.FriendshipStatus.PENDING) {
                throw new BusinessException(400, "已存在待处理的好友请求");
            }
        }

        Friendship f = new Friendship();
        f.setUserId(userId);
        f.setFriendId(dto.getFriendId());
        f.setStatus(Friendship.FriendshipStatus.PENDING);
        f.setSource(dto.getSource() != null ? dto.getSource() : "PROFILE");
        f.setSourceId(dto.getSourceId());
        friendshipRepository.save(f);

        String actorName = userRepository.findById(userId).map(User::getUsername).orElse("");
        String sourceHint = getSourceHint(f.getSource());
        notificationService.create(dto.getFriendId(), "FRIEND_REQUEST", f.getId(), "FRIENDSHIP",
                userId, actorName + "请求加您为好友" + sourceHint);
    }

    @Override
    @Transactional
    public void acceptRequest(Long friendshipId, Long userId) {
        Friendship f = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new BusinessException(404, "好友请求不存在"));
        if (!f.getFriendId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }
        if (f.getStatus() != Friendship.FriendshipStatus.PENDING) {
            throw new BusinessException(400, "该请求已处理");
        }
        f.setStatus(Friendship.FriendshipStatus.ACCEPTED);
        friendshipRepository.save(f);

        String actorName = userRepository.findById(userId).map(User::getUsername).orElse("");
        notificationService.create(f.getUserId(), "FRIEND_ACCEPTED", f.getId(), "FRIENDSHIP",
                userId, actorName + "已接受您的好友请求");
    }

    @Override
    @Transactional
    public void rejectRequest(Long friendshipId, Long userId) {
        Friendship f = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new BusinessException(404, "好友请求不存在"));
        if (!f.getFriendId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }
        if (f.getStatus() != Friendship.FriendshipStatus.PENDING) {
            throw new BusinessException(400, "该请求已处理");
        }
        f.setStatus(Friendship.FriendshipStatus.REJECTED);
        friendshipRepository.save(f);
    }

    @Override
    @Transactional
    public void unfriend(Long friendshipId, Long userId) {
        Friendship f = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new BusinessException(404, "好友关系不存在"));
        if (!f.getUserId().equals(userId) && !f.getFriendId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }
        friendshipRepository.delete(f);
    }

    private String getSourceHint(String source) {
        return switch (source) {
            case "POST" -> "（来自帖子）";
            case "GROUP" -> "（来自群组）";
            default -> "";
        };
    }
}
