package com.snowball.controller;

import com.snowball.common.Result;
import jakarta.validation.Valid;
import com.snowball.dto.FriendRequestDTO;
import com.snowball.service.FriendService;
import com.snowball.vo.FriendVO;
import com.snowball.vo.FriendshipStatusVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/friends")
public class FriendController extends BaseController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @GetMapping
    public Result<List<FriendVO>> getFriends() {
        return Result.success(friendService.getFriends(getCurrentUserId()));
    }

    @GetMapping("/pending")
    public Result<List<FriendVO>> getPendingRequests() {
        return Result.success(friendService.getPendingRequests(getCurrentUserId()));
    }

    @GetMapping("/status/{userId}")
    public Result<FriendshipStatusVO> getStatus(@PathVariable Long userId) {
        return Result.success(friendService.getStatus(getCurrentUserId(), userId));
    }

    @PostMapping("/request")
    public Result<String> sendRequest(@Valid @RequestBody FriendRequestDTO dto) {
        friendService.sendRequest(getCurrentUserId(), dto);
        return Result.success("好友请求已发送");
    }

    @PutMapping("/{friendshipId}/accept")
    public Result<String> acceptRequest(@PathVariable Long friendshipId) {
        friendService.acceptRequest(friendshipId, getCurrentUserId());
        return Result.success("已接受好友请求");
    }

    @PutMapping("/{friendshipId}/reject")
    public Result<String> rejectRequest(@PathVariable Long friendshipId) {
        friendService.rejectRequest(friendshipId, getCurrentUserId());
        return Result.success("已拒绝好友请求");
    }

    @DeleteMapping("/{friendshipId}")
    public Result<String> unfriend(@PathVariable Long friendshipId) {
        friendService.unfriend(friendshipId, getCurrentUserId());
        return Result.success("已删除好友");
    }
}
