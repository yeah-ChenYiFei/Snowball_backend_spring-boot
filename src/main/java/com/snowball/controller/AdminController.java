package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.service.AdminService;
import com.snowball.service.PostService;
import com.snowball.vo.UserVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('SYS_ADMIN')")
public class AdminController extends BaseController {

    private final AdminService adminService;
    private final PostService postService;

    public AdminController(AdminService adminService, PostService postService) {
        this.adminService = adminService;
        this.postService = postService;
    }

    @GetMapping("/stats")
    public Result<Map<String, Long>> getStats() {
        return Result.success(adminService.getStats());
    }

    @GetMapping("/users")
    public Result<List<UserVO>> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(adminService.listUsers(search, role, status, page, size));
    }

    @PutMapping("/users/{id}/role")
    public Result<Void> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminService.updateUserRole(id, body.get("role"), getCurrentUserId());
        return Result.success();
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminService.updateUserStatus(id, body.get("status"));
        return Result.success();
    }

    @DeleteMapping("/posts/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        postService.forceDeletePost(id);
        return Result.success();
    }

    @GetMapping("/articles")
    public Result<List<Map<String, Object>>> listArticles(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(adminService.listArticles(search, status, page, size));
    }

    @PutMapping("/articles/{id}/unpublish")
    public Result<Void> unpublishArticle(@PathVariable Long id) {
        adminService.unpublishArticle(id);
        return Result.success();
    }

    @PutMapping("/articles/{id}/publish")
    public Result<Void> publishArticle(@PathVariable Long id) {
        adminService.publishArticle(id);
        return Result.success();
    }

    @GetMapping("/worlds")
    public Result<List<Map<String, Object>>> listWorlds(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String isPublic,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(adminService.listWorlds(search, isPublic, page, size));
    }

    @DeleteMapping("/worlds/{id}")
    public Result<Void> deleteWorld(@PathVariable Long id) {
        adminService.deleteWorld(id);
        return Result.success();
    }
}
