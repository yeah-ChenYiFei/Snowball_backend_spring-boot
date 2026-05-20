package com.snowball.service;

import com.snowball.vo.PrivateMessageVO;
import com.snowball.vo.UserVO;

import java.util.List;

public interface PrivateMessageService {
    List<PrivateMessageVO> getMessages(Long userId, Long targetUserId, Long sinceId);
    PrivateMessageVO sendMessage(Long senderId, Long receiverId, String body, String imageUrl);
    List<UserVO> getChatPartners(Long userId);
    long getUnreadCount(Long userId);
}
