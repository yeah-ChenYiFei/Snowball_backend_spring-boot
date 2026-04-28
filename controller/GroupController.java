package com.example.snowball.controller;
import com.example.snowball.common.Result;
import com.example.snowball.entity.Group;
import com.example.snowball.entity.GroupMember;
import com.example.snowball.repository.GroupMemberRepository;
import com.example.snowball.repository.GroupRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/v1/groups")
public class GroupController {
    private final GroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    public GroupController(GroupRepository groupRepository, GroupMemberRepository memberRepository) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
    }

    @PostMapping
    public Result<Group> createGroup(@RequestBody Map<String, String> body, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Group group = new Group();
        group.setName(body.get("name"));
        group.setCreatorId(userId);
        groupRepository.save(group);

        // 创建者自动成为群主
        GroupMember member = new GroupMember();
        member.setGroupId(group.getId());
        member.setUserId(userId);
        member.setRole("admin");
        memberRepository.save(member);

        return Result.success(group);
    }

    @GetMapping("/{groupId}/members")
    public Result<List<GroupMember>> getMembers(@PathVariable Long groupId) {
        return Result.success(memberRepository.findByGroupId(groupId));
    }
}
