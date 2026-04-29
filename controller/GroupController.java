package com.example.snowball.controller;

import com.example.snowball.common.Result;
import com.example.snowball.dto.GroupCreateDTO;
import com.example.snowball.service.GroupService;
import com.example.snowball.vo.GroupMemberVO;
import com.example.snowball.vo.GroupVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
public class GroupController extends BaseController { // ✅ 1. 继承基类

    private final GroupService groupService; // ✅ 2. 只找 Service

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public Result<GroupVO> createGroup(@RequestBody GroupCreateDTO dto) { // ✅ 3. 用 DTO 接收
        return Result.success(groupService.createGroup(getCurrentUserId(), dto)); // ✅ 4. 基类拿 ID
    }

    @GetMapping("/{groupId}/members")
    public Result<List<GroupMemberVO>> getMembers(@PathVariable Long groupId) {
        return Result.success(groupService.getMembers(groupId));
    }
}
