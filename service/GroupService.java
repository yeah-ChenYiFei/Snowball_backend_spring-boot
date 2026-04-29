package com.example.snowball.service;

import com.example.snowball.dto.GroupCreateDTO;
import com.example.snowball.vo.GroupMemberVO;
import com.example.snowball.vo.GroupVO;
import java.util.List;

public interface GroupService {
    GroupVO createGroup(Long userId, GroupCreateDTO dto);
    List<GroupMemberVO> getMembers(Long groupId);
}
