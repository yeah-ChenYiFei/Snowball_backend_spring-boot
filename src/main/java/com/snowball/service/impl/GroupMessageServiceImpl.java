package com.snowball.service.impl;

import com.snowball.dto.GroupMessageCreateDTO;
import com.snowball.entity.GroupMessage;
import com.snowball.entity.User;
import com.snowball.repository.GroupMemberRepository;
import com.snowball.repository.GroupMessageRepository;
import com.snowball.repository.UserRepository;
import com.snowball.service.GroupMessageService;
import com.snowball.vo.GroupMessageVO;
import org.springframework.stereotype.Service;

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
    public GroupMessageVO sendMessage(Long groupId, Long userId, GroupMessageCreateDTO dto) {
        memberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new RuntimeException("你不是该群组成员"));

        GroupMessage msg = new GroupMessage();
        msg.setGroupId(groupId);
        msg.setSenderId(userId);
        msg.setBody(dto.getBody() != null ? dto.getBody() : "");
        msg.setType(dto.getType() != null ? dto.getType() : "CHAT");
        msg.setRefId(dto.getRefId());
        msg.setRefType(dto.getRefType());
        msg = messageRepository.save(msg);

        Map<Long, String> usernameMap = userRepository.findAllById(List.of(userId)).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
        return toVO(msg, usernameMap);
    }

    @Override
    public void deleteMessage(Long groupId, Long messageId, Long userId, boolean isAdmin) {
        GroupMessage msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("消息不存在"));
        if (!msg.getGroupId().equals(groupId)) {
            throw new RuntimeException("消息不属于该群组");
        }
        if (!isAdmin && !msg.getSenderId().equals(userId)) {
            throw new RuntimeException("无权删除他人消息");
        }
        messageRepository.delete(msg);
    }

    private List<GroupMessageVO> toVOList(List<GroupMessage> messages) {
        if (messages.isEmpty()) return List.of();

        List<Long> senderIds = messages.stream().map(GroupMessage::getSenderId).distinct().toList();
        Map<Long, String> usernameMap = userRepository.findAllById(senderIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        List<GroupMessageVO> list = new ArrayList<>();
        for (GroupMessage m : messages) {
            list.add(toVO(m, usernameMap));
        }
        return list;
    }

    private GroupMessageVO toVO(GroupMessage m, Map<Long, String> usernameMap) {
        GroupMessageVO vo = new GroupMessageVO();
        vo.setId(m.getId());
        vo.setGroupId(m.getGroupId());
        vo.setSenderId(m.getSenderId());
        vo.setBody(m.getBody());
        vo.setType(m.getType());
        vo.setRefId(m.getRefId());
        vo.setRefType(m.getRefType());
        vo.setCreatedAt(m.getCreatedAt());
        vo.setSenderName(usernameMap.getOrDefault(m.getSenderId(), ""));
        return vo;
    }
}
