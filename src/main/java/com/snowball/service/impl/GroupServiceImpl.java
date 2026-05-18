package com.snowball.service.impl;

import com.snowball.dto.GroupCreateDTO;
import com.snowball.entity.Group;
import com.snowball.entity.GroupMember;
import com.snowball.repository.GroupMemberRepository;
import com.snowball.repository.GroupRepository;
import com.snowball.repository.UserRepository;
import com.snowball.service.GroupService;
import com.snowball.vo.GroupDetailVO;
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

        return toVO(group);
    }

    @Override
    public List<GroupMemberVO> getMembers(Long groupId) {
        List<GroupMember> members = memberRepository.findByGroupId(groupId);
        List<GroupMemberVO> voList = new ArrayList<>();
        for (GroupMember m : members) {
            GroupMemberVO vo = new GroupMemberVO();
            vo.setUserId(m.getUserId());
            vo.setRole(m.getRole());
            userRepository.findById(m.getUserId()).ifPresent(user -> vo.setUsername(user.getUsername()));
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public List<GroupVO> getMyGroups(Long userId) {
        List<Group> groups = groupRepository.findByMemberUserId(userId);
        return groups.stream().map(this::toVO).toList();
    }

    @Override
    public GroupDetailVO getGroupDetail(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("群组不存在"));
        GroupDetailVO vo = new GroupDetailVO();
        vo.setId(group.getId());
        vo.setName(group.getName());
        vo.setDescription(group.getDescription());
        vo.setCreatorId(group.getCreatorId());
        vo.setIsPrivate(group.getIsPrivate());
        vo.setCreatedAt(group.getCreatedAt());
        vo.setMemberCount(memberRepository.countByGroupId(groupId));
        vo.setMembers(getMembers(groupId));
        userRepository.findById(group.getCreatorId()).ifPresent(user -> vo.setCreatorName(user.getUsername()));
        return vo;
    }

    @Override
    public List<GroupVO> searchGroups(String query) {
        if (query == null || query.isBlank()) return List.of();
        return groupRepository.findByNameContainingIgnoreCase(query)
                .stream().map(this::toVO).toList();
    }

    @Override
    @Transactional
    public void joinGroup(Long groupId, Long userId) {
        groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("群组不存在"));
        if (memberRepository.findByGroupIdAndUserId(groupId, userId).isPresent()) {
            throw new RuntimeException("你已经是该群组成员");
        }
        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setRole("member");
        memberRepository.save(member);
    }

    @Override
    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        GroupMember member = memberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new RuntimeException("你不是该群组成员"));
        if ("admin".equals(member.getRole())) {
            throw new RuntimeException("群主不能退出，请先转让群主或解散群组");
        }
        memberRepository.deleteByGroupIdAndUserId(groupId, userId);
    }

    @Override
    @Transactional
    public void kickMember(Long groupId, Long adminId, Long targetUserId) {
        GroupMember adminMember = memberRepository.findByGroupIdAndUserId(groupId, adminId)
                .orElseThrow(() -> new RuntimeException("你不是该群组成员"));
        if (!"admin".equals(adminMember.getRole())) {
            throw new RuntimeException("只有群主可以踢人");
        }
        GroupMember target = memberRepository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new RuntimeException("目标用户不在群中"));
        if ("admin".equals(target.getRole())) {
            throw new RuntimeException("不能踢出群主");
        }
        memberRepository.deleteByGroupIdAndUserId(groupId, targetUserId);
    }

    @Override
    @Transactional
    public void deleteGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("群组不存在"));
        if (!group.getCreatorId().equals(userId)) {
            throw new RuntimeException("只有群主可以解散群组");
        }
        groupRepository.delete(group);
    }

    private GroupVO toVO(Group group) {
        GroupVO vo = new GroupVO();
        vo.setId(group.getId());
        vo.setName(group.getName());
        vo.setDescription(group.getDescription());
        vo.setCreatorId(group.getCreatorId());
        vo.setIsPrivate(group.getIsPrivate());
        vo.setCreatedAt(group.getCreatedAt());
        vo.setMemberCount(memberRepository.countByGroupId(group.getId()));
        userRepository.findById(group.getCreatorId()).ifPresent(user -> vo.setCreatorName(user.getUsername()));
        return vo;
    }
}
