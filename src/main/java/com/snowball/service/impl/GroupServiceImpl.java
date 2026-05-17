package com.snowball.service.impl;

import com.snowball.dto.GroupCreateDTO;
import com.snowball.entity.Group;
import com.snowball.entity.GroupMember;
import com.snowball.repository.GroupMemberRepository;
import com.snowball.repository.GroupRepository;
import com.snowball.repository.UserRepository;
import com.snowball.service.GroupService;
import com.snowball.vo.GroupMemberVO;
import com.snowball.vo.GroupVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final UserRepository userRepository;

    public GroupServiceImpl(GroupRepository groupRepository, GroupMemberRepository memberRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public GroupVO createGroup(Long userId, GroupCreateDTO dto) {
        Group group = new Group();
        group.setName(dto.getName());
        group.setDescription(dto.getDescription());
        group.setCreatorId(userId);
        groupRepository.save(group);

        GroupMember member = new GroupMember();
        member.setGroupId(group.getId());
        member.setUserId(userId);
        member.setRole("admin");
        memberRepository.save(member);

        GroupVO vo = new GroupVO();
        vo.setId(group.getId());
        vo.setName(group.getName());
        vo.setDescription(group.getDescription());
        vo.setCreatorId(group.getCreatorId());
        vo.setIsPrivate(group.getIsPrivate());
        vo.setCreatedAt(group.getCreatedAt());
        vo.setMemberCount(1L);

        userRepository.findById(userId).ifPresent(user -> vo.setCreatorName(user.getUsername()));
        return vo;
    }

    @Override
    public List<GroupMemberVO> getMembers(Long groupId) {
        List<GroupMember> members = memberRepository.findByGroupId(groupId);
        List<GroupMemberVO> voList = new ArrayList<>();

        for (GroupMember m : members) {
            GroupMemberVO vo = new GroupMemberVO();
            vo.setUserId(m.getUserId());
            vo.setRole(m.getRole());
            userRepository.findById(m.getUserId()).ifPresent(user -> {
                vo.setUsername(user.getUsername());
            });
            voList.add(vo);
        }
        return voList;
    }
}
