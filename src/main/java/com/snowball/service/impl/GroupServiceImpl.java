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
        groupRepository.save(group);

        GroupMember member = new GroupMember();
        member.setGroupId(group.getId());
        member.setUserId(userId);
        member.setRole("admin");
        memberRepository.save(member);

        Map<Long, String> usernameMap = userRepository.findAllById(List.of(userId)).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
        return toVO(group, Map.of(group.getId(), 1L), usernameMap);
    }

    @Override
    public List<GroupMemberVO> getMembers(Long groupId) {
        List<GroupMember> members = memberRepository.findByGroupId(groupId);
        if (members.isEmpty()) return List.of();

        List<Long> userIds = members.stream().map(GroupMember::getUserId).distinct().toList();
        Map<Long, String> usernameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        List<GroupMemberVO> voList = new ArrayList<>();
        for (GroupMember m : members) {
            GroupMemberVO vo = new GroupMemberVO();
            vo.setUserId(m.getUserId());
            vo.setRole(m.getRole());
            vo.setUsername(usernameMap.getOrDefault(m.getUserId(), ""));
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
        vo.setCreatedAt(group.getCreatedAt());
        vo.setMemberCount(memberRepository.countByGroupId(groupId));
        vo.setMembers(getMembers(groupId));
        userRepository.findById(group.getCreatorId()).ifPresent(user -> vo.setCreatorName(user.getUsername()));
        return vo;
    }

    @Override
    public List<GroupVO> searchGroups(String query) {
        if (query == null || query.isBlank()) return List.of();
        List<Group> groups = groupRepository.findByNameContainingIgnoreCase(query);
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

        // Batch member counts
        Map<Long, Long> countMap = memberRepository.countByGroupIdIn(groupIds).stream()
                .collect(Collectors.toMap(row -> ((Number) row[0]).longValue(), row -> ((Number) row[1]).longValue()));

        // Batch usernames
        Map<Long, String> usernameMap = userRepository.findAllById(creatorIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        return groups.stream().map(g -> toVO(g, countMap, usernameMap)).toList();
    }

    private GroupVO toVO(Group group, Map<Long, Long> countMap, Map<Long, String> usernameMap) {
        GroupVO vo = new GroupVO();
        vo.setId(group.getId());
        vo.setName(group.getName());
        vo.setDescription(group.getDescription());
        vo.setCreatorId(group.getCreatorId());
        vo.setIsPrivate(group.getIsPrivate());
        vo.setCreatedAt(group.getCreatedAt());
        vo.setMemberCount(countMap.getOrDefault(group.getId(), 0L));
        vo.setCreatorName(usernameMap.getOrDefault(group.getCreatorId(), ""));
        return vo;
    }
}
