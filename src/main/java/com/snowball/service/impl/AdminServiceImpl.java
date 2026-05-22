package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.entity.Article;
import com.snowball.entity.User;
import com.snowball.repository.*;
import com.snowball.service.AdminService;
import com.snowball.vo.UserVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ArticleRepository articleRepository;
    private final WorldRepository worldRepository;
    private final StoryChainRepository storyChainRepository;
    private final GroupRepository groupRepository;
    private final CommentRepository commentRepository;

    @Value("${snowball.root-admin-id:1}")
    private Long rootAdminId;

    public AdminServiceImpl(UserRepository userRepository, PostRepository postRepository,
                            ArticleRepository articleRepository, WorldRepository worldRepository,
                            StoryChainRepository storyChainRepository, GroupRepository groupRepository,
                            CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.articleRepository = articleRepository;
        this.worldRepository = worldRepository;
        this.storyChainRepository = storyChainRepository;
        this.groupRepository = groupRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("users", userRepository.count());
        stats.put("posts", postRepository.count());
        stats.put("articles", articleRepository.count());
        stats.put("worlds", worldRepository.count());
        stats.put("chains", storyChainRepository.count());
        stats.put("groups", groupRepository.count());
        stats.put("comments", commentRepository.count());
        return stats;
    }

    @Override
    public List<UserVO> listUsers(String search, String role, String status, int page, int size) {
        List<User> users;
        if (search != null && !search.isBlank()) {
            users = userRepository.findByUsernameContaining(search);
        } else {
            users = userRepository.findAll();
        }

        return users.stream()
                .filter(u -> role == null || role.isBlank() || u.getRole().name().equals(role))
                .filter(u -> status == null || status.isBlank() || u.getStatus().name().equals(status))
                .skip((long) (page - 1) * size)
                .limit(size)
                .map(this::toVO)
                .toList();
    }

    @Override
    public void updateUserRole(Long userId, String role, Long operatorId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        boolean isRoot = operatorId.equals(rootAdminId);

        // Root admin: cannot be modified by anyone (including themselves)
        if (user.getId().equals(rootAdminId)) {
            throw new BusinessException(403, "不能修改根管理员的角色");
        }

        // Non-root cannot touch a SYS_ADMIN at all
        if (!isRoot && user.getRole() == User.UserRole.SYS_ADMIN) {
            throw new BusinessException(403, "只有根管理员才能修改超级管理员的角色");
        }

        // Non-root cannot promote anyone to SYS_ADMIN
        if (!isRoot && "SYS_ADMIN".equals(role)) {
            throw new BusinessException(403, "只有根管理员才能提权至超级管理员");
        }

        try {
            user.setRole(User.UserRole.valueOf(role));
        } catch (IllegalArgumentException e) {
            throw new BusinessException(400, "无效的角色: " + role);
        }
        userRepository.save(user);
    }

    @Override
    public void updateUserStatus(Long userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        try {
            user.setStatus(User.UserStatus.valueOf(status));
        } catch (IllegalArgumentException e) {
            throw new BusinessException(400, "无效的状态: " + status);
        }
        userRepository.save(user);
    }

    @Override
    public List<Map<String, Object>> listArticles(String search, String status, int page, int size) {
        return articleRepository.findAll().stream()
                .filter(a -> status == null || status.isBlank() || a.getStatus().equals(status))
                .filter(a -> search == null || search.isBlank() || a.getTitle().toLowerCase().contains(search.toLowerCase()))
                .skip((long) (page - 1) * size)
                .limit(size)
                .map(a -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", a.getId());
                    m.put("title", a.getTitle());
                    m.put("status", a.getStatus());
                    m.put("userId", a.getUserId());
                    m.put("createdAt", a.getCreatedAt());
                    return m;
                })
                .toList();
    }

    @Override
    public List<Map<String, Object>> listWorlds(String search, String isPublic, int page, int size) {
        return worldRepository.findAll().stream()
                .filter(w -> isPublic == null || isPublic.isBlank() || w.getIsPublic().toString().equals(isPublic))
                .filter(w -> search == null || search.isBlank() || w.getName().toLowerCase().contains(search.toLowerCase()))
                .skip((long) (page - 1) * size)
                .limit(size)
                .map(w -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", w.getId());
                    m.put("name", w.getName());
                    m.put("isPublic", w.getIsPublic());
                    m.put("userId", w.getUserId());
                    m.put("createdAt", w.getCreatedAt());
                    return m;
                })
                .toList();
    }

    @Override
    public void unpublishArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "文章不存在"));
        article.setStatus("HIDDEN");
        article.setIsPublished(false);
        articleRepository.save(article);
    }

    @Override
    public void publishArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "文章不存在"));
        article.setStatus("PUBLISHED");
        article.setIsPublished(true);
        articleRepository.save(article);
    }

    @Override
    public void deleteWorld(Long id) {
        worldRepository.deleteById(id);
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setRole(user.getRole().name());
        vo.setStatus(user.getStatus().name());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }
}
