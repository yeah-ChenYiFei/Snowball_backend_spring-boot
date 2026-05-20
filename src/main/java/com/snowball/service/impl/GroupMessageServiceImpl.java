package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.dto.GroupMessageCreateDTO;
import com.snowball.entity.GroupMessage;
import com.snowball.entity.User;
import com.snowball.repository.GroupMemberRepository;
import com.snowball.repository.GroupMessageRepository;
import com.snowball.repository.UserRepository;
import com.snowball.service.GroupMessageService;
import com.snowball.vo.GroupMessageVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GroupMessageServiceImpl implements GroupMessageService {

    private final GroupMessageRepository messageRepository;
    private final GroupMemberRepository memberRepository;
    private final UserRepository userRepository;

    public GroupMessageServiceImpl(GroupMessageRepository messageRepository,
                                   GroupMemberRepository memberRepository,
                                   UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<GroupMessageVO> getMessages(Long groupId, Long sinceId) {
        List<GroupMessage> messages;
        if (sinceId != null && sinceId > 0) {
            messages = messageRepository.findByGroupIdAndIdGreaterThanOrderByCreatedAtAsc(groupId, sinceId);
        } else {
            messages = messageRepository.findByGroupIdOrderByCreatedAtAsc(groupId);
        }
        return toVOList(messages);
    }

    @Override
    @Transactional
    public GroupMessageVO sendMessage(Long groupId, Long userId, GroupMessageCreateDTO dto) {
        memberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new BusinessException(403, "你不是该群组成员"));

        GroupMessage msg = new GroupMessage();
        msg.setGroupId(groupId);
        msg.setSenderId(userId);
        msg.setBody(dto.getBody() != null ? dto.getBody() : "");
        msg.setImageUrl(dto.getImageUrl());
        msg.setType(dto.getType() != null ? GroupMessage.MessageType.valueOf(dto.getType()) : GroupMessage.MessageType.CHAT);
        msg.setRefId(dto.getRefId());
        msg.setRefType(dto.getRefType() != null ? GroupMessage.RefType.valueOf(dto.getRefType()) : null);
        msg = messageRepository.save(msg);

        Map<Long, User> userMap = userRepository.findAllById(List.of(userId)).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        return toVO(msg, userMap);
    }

    @Override
    @Transactional
    public void deleteMessage(Long groupId, Long messageId, Long userId, boolean isAdmin) {
        GroupMessage msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(404, "消息不存在"));
        if (!msg.getGroupId().equals(groupId)) {
            throw new BusinessException(400, "消息不属于该群组");
        }
        if (!isAdmin && !msg.getSenderId().equals(userId)) {
            throw new BusinessException(403, "无权删除他人消息");
        }
        messageRepository.delete(msg);
    }

    private List<GroupMessageVO> toVOList(List<GroupMessage> messages) {
        if (messages.isEmpty()) return List.of();

        List<Long> senderIds = messages.stream().map(GroupMessage::getSenderId).distinct().toList();
        Map<Long, User> userMap = userRepository.findAllById(senderIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<GroupMessageVO> list = new ArrayList<>();
        for (GroupMessage m : messages) {
            list.add(toVO(m, userMap));
        }
        return list;
    }

    private GroupMessageVO toVO(GroupMessage m, Map<Long, User> userMap) {
        User sender = userMap.get(m.getSenderId());
        GroupMessageVO vo = new GroupMessageVO();
        vo.setId(m.getId());
        vo.setGroupId(m.getGroupId());
        vo.setSenderId(m.getSenderId());
        vo.setBody(m.getBody());
        vo.setImageUrl(m.getImageUrl());
        vo.setSenderAvatarUrl(sender != null ? sender.getAvatarUrl() : null);
        vo.setType(m.getType().name());
        vo.setRefId(m.getRefId());
        vo.setRefType(m.getRefType() != null ? m.getRefType().name() : null);
        vo.setCreatedAt(m.getCreatedAt());
        vo.setSenderName(sender != null ? sender.getUsername() : "");
        return vo;
    }
}
