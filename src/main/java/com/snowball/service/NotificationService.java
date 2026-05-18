package com.snowball.service;

import com.snowball.vo.NotificationVO;

import java.util.List;

public interface NotificationService {
    List<NotificationVO> getNotifications(Long userId);
    long getUnreadCount(Long userId);
    void markRead(Long notificationId, Long userId);
    void markAllRead(Long userId);

    void create(Long userId, String type, Long sourceId, String sourceType, Long actorId, String body);
}
