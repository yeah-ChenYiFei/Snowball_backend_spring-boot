package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.dto.GroupCreateDTO;
import com.snowball.entity.Group;
import com.snowball.entity.GroupMember;
import com.snowball.entity.User;
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
import java.util.Map;
import java.util.stream.Collectors;

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
        group.setAvatarUrl(dto.getAvatarUrl());
        group.setGroupNumber(groupRepository.findMaxGroupNumber().orElse(0L) + 1);
        groupRepository.save(group);

        GroupMember member = new GroupMember();
        member.setGroupId(group.getId());
        member.setUserId(userId);
        member.setRole("admin");
        memberRepository.save(member);

        Map<Long, User> userMap = userRepository.findAllById(List.of(userId)).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        return toVO(group, Map.of(group.getId(), 1L), userMap);
    }

    @Override
    public List<GroupMemberVO> getMembers(Long groupId) {
        List<GroupMember> members = memberRepository.findByGroupId(groupId);
        if (members.isEmpty()) return List.of();

        List<Long> userIds = members.stream().map(GroupMember::getUserId).distinct().toList();
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<GroupMemberVO> voList = new ArrayList<>();
        for (GroupMember m : members) {
            User u = userMap.get(m.getUserId());
            GroupMemberVO vo = new GroupMemberVO();
            vo.setUserId(m.getUserId());
            vo.setRole(m.getRole());
            vo.setUsername(u != null ? u.getUsername() : "");
            vo.setAvatarUrl(u != null ? u.getAvatarUrl() : null);
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public List<GroupVO> getMyGroups(Long userId) {
        List<Group> groups = groupRepository.findByMemberUserId(userId);
        return batchToVOList(groups);
    }

    @Override
    public GroupDetailVO getGroupDetail(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(404, "群组不存在"));
        GroupDetailVO vo = new GroupDetailVO();
        vo.setId(group.getId());
        vo.setName(group.getName());
        vo.setDescription(group.getDescription());
        vo.setCreatorId(group.getCreatorId());
        vo.setIsPrivate(group.getIsPrivate());
        vo.setIsSearchable(group.getIsSearchable());
        vo.setAvatarUrl(group.getAvatarUrl());
        vo.setGroupNumber(group.getGroupNumber());
        vo.setCreatedAt(group.getCreatedAt());
        vo.setMemberCount(memberRepository.countByGroupId(groupId));
        vo.setMembers(getMembers(groupId));
        userRepository.findById(group.getCreatorId()).ifPresent(user -> {
            vo.setCreatorName(user.getUsername());
        });
        return vo;
    }

    @Override
    public List<GroupVO> searchGroups(String query) {
        if (query == null || query.isBlank()) return List.of();
        List<Group> groups = groupRepository.findByNameContainingIgnoreCaseAndIsSearchableTrue(query);
        return batchToVOList(groups);
    }

    @Override
    @Transactional
    public void joinGroup(Long groupId, Long userId) {
        groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(404, "群组不存在"));
        if (memberRepository.findByGroupIdAndUserId(groupId, userId).isPresent()) {
            throw new BusinessException(400, "你已经是该群组成员");
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
                .orElseThrow(() -> new BusinessException(404, "你不是该群组成员"));
        if ("admin".equals(member.getRole())) {
            throw new BusinessException(400, "群主不能退出，请先转让群主或解散群组");
        }
        memberRepository.deleteByGroupIdAndUserId(groupId, userId);
    }

    @Override
    @Transactional
    public void kickMember(Long groupId, Long adminId, Long targetUserId) {
        GroupMember adminMember = memberRepository.findByGroupIdAndUserId(groupId, adminId)
                .orElseThrow(() -> new BusinessException(404, "你不是该群组成员"));
        if (!"admin".equals(adminMember.getRole())) {
            throw new BusinessException(403, "只有群主可以踢人");
        }
        GroupMember target = memberRepository.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new BusinessException(404, "目标用户不在群中"));
        if ("admin".equals(target.getRole())) {
            throw new BusinessException(400, "不能踢出群主");
        }
        memberRepository.deleteByGroupIdAndUserId(groupId, targetUserId);
    }

    @Override
    @Transactional
    public void deleteGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(404, "群组不存在"));
        if (!group.getCreatorId().equals(userId)) {
            throw new BusinessException(403, "只有群主可以解散群组");
        }
        groupRepository.delete(group);
    }

    private List<GroupVO> batchToVOList(List<Group> groups) {
        if (groups.isEmpty()) return List.of();

        List<Long> groupIds = groups.stream().map(Group::getId).toList();
        List<Long> creatorIds = groups.stream().map(Group::getCreatorId).distinct().toList();

        Map<Long, Long> countMap = memberRepository.countByGroupIdIn(groupIds).stream()
                .collect(Collectors.toMap(row -> ((Number) row[0]).longValue(), row -> ((Number) row[1]).longValue()));

        Map<Long, User> userMap = userRepository.findAllById(creatorIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return groups.stream().map(g -> toVO(g, countMap, userMap)).toList();
    }

    private GroupVO toVO(Group group, Map<Long, Long> countMap, Map<Long, User> userMap) {
        User creator = userMap.get(group.getCreatorId());
        GroupVO vo = new GroupVO();
        vo.setId(group.getId());
        vo.setName(group.getName());
        vo.setDescription(group.getDescription());
        vo.setCreatorId(group.getCreatorId());
        vo.setCreatorName(creator != null ? creator.getUsername() : "");
        vo.setIsPrivate(group.getIsPrivate());
        vo.setIsSearchable(group.getIsSearchable());
        vo.setAvatarUrl(group.getAvatarUrl());
        vo.setGroupNumber(group.getGroupNumber());
        vo.setMemberCount(countMap.getOrDefault(group.getId(), 0L));
        vo.setCreatedAt(group.getCreatedAt());
        return vo;
    }

    @Override
    @Transactional
    public void updateGroup(Long groupId, Long userId, String name, String description, String avatarUrl, Boolean isSearchable) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(404, "群组不存在"));
        if (!group.getCreatorId().equals(userId)) {
            throw new BusinessException(403, "只有群主可以修改群组信息");
        }
        if (name != null) group.setName(name);
        if (description != null) group.setDescription(description);
        if (avatarUrl != null) group.setAvatarUrl(avatarUrl);
        if (isSearchable != null) group.setIsSearchable(isSearchable);
        groupRepository.save(group);
    }
}
