package com.snowball.service.impl;

import com.snowball.entity.PrivateMessage;
import com.snowball.entity.User;
import com.snowball.repository.PrivateMessageRepository;
import com.snowball.repository.UserRepository;
import com.snowball.service.PrivateMessageService;
import com.snowball.vo.PrivateMessageVO;
import com.snowball.vo.UserVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PrivateMessageServiceImpl implements PrivateMessageService {

    private final PrivateMessageRepository messageRepository;
    private final UserRepository userRepository;

    public PrivateMessageServiceImpl(PrivateMessageRepository messageRepository,
                                      UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public List<PrivateMessageVO> getMessages(Long userId, Long targetUserId, Long sinceId) {
        List<PrivateMessage> messages;
        if (sinceId != null && sinceId > 0) {
            messages = messageRepository.findConversationSince(userId, targetUserId, sinceId);
        } else {
            messages = messageRepository.findConversation(userId, targetUserId);
        }

        // Mark messages sent to current user as read
        for (PrivateMessage m : messages) {
            if (m.getReceiverId().equals(userId) && !m.getIsRead()) {
                m.setIsRead(true);
                messageRepository.save(m);
            }
        }

        return toVOList(messages);
    }

    @Override
    @Transactional
    public PrivateMessageVO sendMessage(Long senderId, Long receiverId, String body, String imageUrl) {
        if (senderId.equals(receiverId)) {
            throw new RuntimeException("不能给自己发消息");
        }

        PrivateMessage msg = new PrivateMessage();
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setBody(body != null ? body : "");
        msg.setImageUrl(imageUrl);
        msg.setIsRead(false);
        msg = messageRepository.save(msg);

        PrivateMessageVO vo = new PrivateMessageVO();
        vo.setId(msg.getId());
        vo.setSenderId(msg.getSenderId());
        vo.setReceiverId(msg.getReceiverId());
        vo.setBody(msg.getBody());
        vo.setImageUrl(msg.getImageUrl());
        vo.setIsRead(msg.getIsRead());
        vo.setCreatedAt(msg.getCreatedAt());
        userRepository.findById(senderId).ifPresent(u -> vo.setSenderName(u.getUsername()));
        return vo;
    }

    @Override
    public List<UserVO> getChatPartners(Long userId) {
        List<Long> partnerIds = messageRepository.findChatPartnerIds(userId);
        if (partnerIds.isEmpty()) return List.of();

        return userRepository.findAllById(partnerIds).stream().map(u -> {
            UserVO vo = new UserVO();
            vo.setId(u.getId());
            vo.setUsername(u.getUsername());
            vo.setAvatarUrl(u.getAvatarUrl());
            return vo;
        }).toList();
    }

    @Override
    public long getUnreadCount(Long userId) {
        return messageRepository.countByReceiverIdAndIsReadFalse(userId);
    }

    private List<PrivateMessageVO> toVOList(List<PrivateMessage> messages) {
        if (messages.isEmpty()) return List.of();

        List<Long> senderIds = messages.stream().map(PrivateMessage::getSenderId).distinct().toList();
        Map<Long, String> nameMap = userRepository.findAllById(senderIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        return messages.stream().map(m -> {
            PrivateMessageVO vo = new PrivateMessageVO();
            vo.setId(m.getId());
            vo.setSenderId(m.getSenderId());
            vo.setReceiverId(m.getReceiverId());
            vo.setSenderName(nameMap.getOrDefault(m.getSenderId(), ""));
            vo.setBody(m.getBody());
            vo.setImageUrl(m.getImageUrl());
            vo.setIsRead(m.getIsRead());
            vo.setCreatedAt(m.getCreatedAt());
            return vo;
        }).toList();
    }
}
