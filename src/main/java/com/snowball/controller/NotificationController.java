package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.service.NotificationService;
import com.snowball.vo.NotificationVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController extends BaseController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public Result<List<NotificationVO>> getNotifications() {
        return Result.success(notificationService.getNotifications(getCurrentUserId()));
    }

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        return Result.success(notificationService.getUnreadCount(getCurrentUserId()));
    }

    @PutMapping("/{id}/read")
    public Result<String> markRead(@PathVariable Long id) {
        notificationService.markRead(id, getCurrentUserId());
        return Result.success("ok");
    }

    @PutMapping("/read-all")
    public Result<String> markAllRead() {
        notificationService.markAllRead(getCurrentUserId());
        return Result.success("ok");
    }
}
