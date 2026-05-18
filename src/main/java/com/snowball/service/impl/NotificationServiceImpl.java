package com.snowball.service.impl;

import com.snowball.entity.Notification;
import com.snowball.entity.User;
import com.snowball.repository.NotificationRepository;
import com.snowball.repository.UserRepository;
import com.snowball.service.NotificationService;
import com.snowball.vo.NotificationVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<NotificationVO> getNotifications(Long userId) {
        List<Notification> list = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (list.isEmpty()) return List.of();

        List<Long> actorIds = list.stream().map(Notification::getActorId).filter(id -> id != null).distinct().toList();
        Map<Long, String> usernameMap = userRepository.findAllById(actorIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        return list.stream().map(n -> {
            NotificationVO vo = new NotificationVO();
            vo.setId(n.getId());
            vo.setUserId(n.getUserId());
            vo.setType(n.getType());
            vo.setSourceId(n.getSourceId());
            vo.setSourceType(n.getSourceType());
            vo.setActorId(n.getActorId());
            vo.setActorName(usernameMap.getOrDefault(n.getActorId(), ""));
            vo.setBody(n.getBody());
            vo.setIsRead(n.getIsRead());
            vo.setCreatedAt(n.getCreatedAt());
            return vo;
        }).toList();
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public void markRead(Long notificationId, Long userId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("通知不存在"));
        if (!n.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }
        n.setIsRead(true);
        notificationRepository.save(n);
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllRead(userId);
    }

    @Override
    public void create(Long userId, String type, Long sourceId, String sourceType, Long actorId, String body) {
        // Don't notify users about their own actions
        if (actorId != null && actorId.equals(userId)) return;

        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setSourceId(sourceId);
        n.setSourceType(sourceType);
        n.setActorId(actorId);
        n.setBody(body);
        notificationRepository.save(n);
    }
}
