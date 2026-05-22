package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.dto.FriendRequestDTO;
import com.snowball.entity.Friendship;
import com.snowball.entity.User;
import com.snowball.repository.FriendshipRepository;
import com.snowball.repository.UserRepository;
import com.snowball.service.NotificationService;
import com.snowball.vo.FriendshipStatusVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendServiceImplTest {

    @Mock FriendshipRepository friendshipRepository;
    @Mock UserRepository userRepository;
    @Mock NotificationService notificationService;

    @InjectMocks FriendServiceImpl friendService;

    // ===== sendRequest =====

    @Test
    void sendRequest_self_throws400() {
        FriendRequestDTO dto = new FriendRequestDTO();
        dto.setFriendId(1L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> friendService.sendRequest(1L, dto));
        assertEquals(400, ex.getCode());
        assertTrue(ex.getMessage().contains("自己"));
    }

    @Test
    void sendRequest_alreadyAccepted_throws400() {
        Friendship accepted = new Friendship();
        accepted.setStatus(Friendship.FriendshipStatus.ACCEPTED);

        when(friendshipRepository.findAnyFriendship(1L, 2L)).thenReturn(Optional.of(accepted));

        FriendRequestDTO dto = new FriendRequestDTO();
        dto.setFriendId(2L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> friendService.sendRequest(1L, dto));
        assertEquals(400, ex.getCode());
        assertTrue(ex.getMessage().contains("已经是好友"));
    }

    @Test
    void sendRequest_alreadyPending_throws400() {
        Friendship pending = new Friendship();
        pending.setStatus(Friendship.FriendshipStatus.PENDING);

        when(friendshipRepository.findAnyFriendship(1L, 2L)).thenReturn(Optional.of(pending));

        FriendRequestDTO dto = new FriendRequestDTO();
        dto.setFriendId(2L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> friendService.sendRequest(1L, dto));
        assertEquals(400, ex.getCode());
        assertTrue(ex.getMessage().contains("待处理"));
    }

    @Test
    void sendRequest_previouslyRejected_reusesAndResends() {
        Friendship rejected = new Friendship();
        rejected.setStatus(Friendship.FriendshipStatus.REJECTED);
        rejected.setUserId(2L); // was originally from user 2 to user 1
        rejected.setFriendId(1L);

        when(friendshipRepository.findAnyFriendship(1L, 2L)).thenReturn(Optional.of(rejected));
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User() {{ setId(1L); setUsername("alice"); }}));

        FriendRequestDTO dto = new FriendRequestDTO();
        dto.setFriendId(2L);
        dto.setSource("POST");

        friendService.sendRequest(1L, dto);

        // should have updated the existing rejected record
        verify(friendshipRepository).save(rejected);
        assertEquals(Friendship.FriendshipStatus.PENDING, rejected.getStatus());
        assertEquals(1L, rejected.getUserId());
        assertEquals(2L, rejected.getFriendId());
    }

    @Test
    void sendRequest_new_createsPending() {
        when(friendshipRepository.findAnyFriendship(1L, 2L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User() {{ setId(1L); setUsername("alice"); }}));

        FriendRequestDTO dto = new FriendRequestDTO();
        dto.setFriendId(2L);

        friendService.sendRequest(1L, dto);

        verify(friendshipRepository).save(argThat(f ->
                f.getStatus() == Friendship.FriendshipStatus.PENDING &&
                        f.getUserId().equals(1L) &&
                        f.getFriendId().equals(2L)));
    }

    // ===== getStatus =====

    @Test
    void getStatus_self_returnsSELF() {
        FriendshipStatusVO result = friendService.getStatus(1L, 1L);
        assertEquals("SELF", result.getStatus());
    }

    @Test
    void getStatus_friend_returnsFRIEND() {
        when(friendshipRepository.findAcceptedFriendship(1L, 2L))
                .thenReturn(Optional.of(new Friendship()));

        FriendshipStatusVO result = friendService.getStatus(1L, 2L);
        assertEquals("FRIEND", result.getStatus());
    }

    @Test
    void getStatus_pendingFromMe_returnsPENDING_TO_THEM() {
        when(friendshipRepository.findAcceptedFriendship(1L, 2L)).thenReturn(Optional.empty());

        Friendship pending = new Friendship();
        pending.setUserId(1L);
        pending.setStatus(Friendship.FriendshipStatus.PENDING);
        when(friendshipRepository.findAnyFriendship(1L, 2L)).thenReturn(Optional.of(pending));

        FriendshipStatusVO result = friendService.getStatus(1L, 2L);
        assertEquals("PENDING_TO_THEM", result.getStatus());
    }

    @Test
    void getStatus_pendingFromThem_returnsPENDING_FROM_THEM() {
        when(friendshipRepository.findAcceptedFriendship(1L, 2L)).thenReturn(Optional.empty());

        Friendship pending = new Friendship();
        pending.setUserId(2L); // friend is requester
        pending.setStatus(Friendship.FriendshipStatus.PENDING);
        when(friendshipRepository.findAnyFriendship(1L, 2L)).thenReturn(Optional.of(pending));

        FriendshipStatusVO result = friendService.getStatus(1L, 2L);
        assertEquals("PENDING_FROM_THEM", result.getStatus());
    }

    @Test
    void getStatus_none_returnsNONE() {
        when(friendshipRepository.findAcceptedFriendship(1L, 2L)).thenReturn(Optional.empty());
        when(friendshipRepository.findAnyFriendship(1L, 2L)).thenReturn(Optional.empty());

        FriendshipStatusVO result = friendService.getStatus(1L, 2L);
        assertEquals("NONE", result.getStatus());
    }

    // ===== acceptRequest =====

    @Test
    void acceptRequest_notRecipient_throws403() {
        Friendship f = new Friendship();
        f.setFriendId(1L); // intended recipient is user 1
        f.setStatus(Friendship.FriendshipStatus.PENDING);

        when(friendshipRepository.findById(1L)).thenReturn(Optional.of(f));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> friendService.acceptRequest(1L, 999L));
        assertEquals(403, ex.getCode());
    }

    @Test
    void rejectRequest_notRecipient_throws403() {
        Friendship f = new Friendship();
        f.setFriendId(1L);
        f.setStatus(Friendship.FriendshipStatus.PENDING);

        when(friendshipRepository.findById(1L)).thenReturn(Optional.of(f));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> friendService.rejectRequest(1L, 999L));
        assertEquals(403, ex.getCode());
    }
}
