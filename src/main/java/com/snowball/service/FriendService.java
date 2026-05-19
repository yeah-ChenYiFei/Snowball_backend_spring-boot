package com.snowball.service;

import com.snowball.dto.FriendRequestDTO;
import com.snowball.vo.FriendVO;
import com.snowball.vo.FriendshipStatusVO;

import java.util.List;

public interface FriendService {
    List<FriendVO> getFriends(Long userId);
    List<FriendVO> getPendingRequests(Long userId);
    FriendshipStatusVO getStatus(Long userId, Long targetUserId);
    void sendRequest(Long userId, FriendRequestDTO dto);
    void acceptRequest(Long friendshipId, Long userId);
    void rejectRequest(Long friendshipId, Long userId);
    void unfriend(Long friendshipId, Long userId);
}
