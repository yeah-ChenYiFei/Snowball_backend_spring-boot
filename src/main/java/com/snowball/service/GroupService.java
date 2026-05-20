package com.snowball.service;

import com.snowball.dto.GroupCreateDTO;
import com.snowball.vo.GroupDetailVO;
import com.snowball.vo.GroupMemberVO;
import com.snowball.vo.GroupVO;
import java.util.List;

public interface GroupService {
    GroupVO createGroup(Long userId, GroupCreateDTO dto);
    List<GroupMemberVO> getMembers(Long groupId);
    List<GroupVO> getMyGroups(Long userId);
    GroupDetailVO getGroupDetail(Long groupId);
    List<GroupVO> searchGroups(String query);
    void joinGroup(Long groupId, Long userId);
    void leaveGroup(Long groupId, Long userId);
    void kickMember(Long groupId, Long adminId, Long targetUserId);
    void deleteGroup(Long groupId, Long userId);
    void updateGroup(Long groupId, Long userId, String name, String description, String avatarUrl, Boolean isSearchable);
}
