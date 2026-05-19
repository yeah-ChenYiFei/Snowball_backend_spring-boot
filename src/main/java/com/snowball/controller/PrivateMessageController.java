package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.dto.PrivateMessageCreateDTO;
import com.snowball.service.PrivateMessageService;
import com.snowball.vo.PrivateMessageVO;
import com.snowball.vo.UserVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
public class PrivateMessageController extends BaseController {

    private final PrivateMessageService messageService;

    public PrivateMessageController(PrivateMessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public Result<List<UserVO>> getChatPartners() {
        return Result.success(messageService.getChatPartners(getCurrentUserId()));
    }

    @GetMapping("/{targetUserId}")
    public Result<List<PrivateMessageVO>> getMessages(
            @PathVariable Long targetUserId,
            @RequestParam(required = false) Long since) {
        return Result.success(messageService.getMessages(getCurrentUserId(), targetUserId, since));
    }

    @PostMapping("/{targetUserId}")
    public Result<PrivateMessageVO> sendMessage(
            @PathVariable Long targetUserId,
            @RequestBody PrivateMessageCreateDTO dto) {
        return Result.success(messageService.sendMessage(getCurrentUserId(), targetUserId, dto.getBody()));
    }

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        return Result.success(messageService.getUnreadCount(getCurrentUserId()));
    }
}
