package com.snowball.service;

import com.snowball.dto.GroupCreateDTO;
import com.snowball.vo.GroupMemberVO;
import com.snowball.vo.GroupVO;
import java.util.List;

public interface GroupService {
    GroupVO createGroup(Long userId, GroupCreateDTO dto);
    List<GroupMemberVO> getMembers(Long groupId);
}
