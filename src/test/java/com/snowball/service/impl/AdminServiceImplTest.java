package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.entity.Article;
import com.snowball.entity.User;
import com.snowball.repository.ArticleRepository;
import com.snowball.repository.CommentRepository;
import com.snowball.repository.GroupRepository;
import com.snowball.repository.PostRepository;
import com.snowball.repository.StoryChainRepository;
import com.snowball.repository.UserRepository;
import com.snowball.repository.WorldRepository;
import com.snowball.vo.UserVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock PostRepository postRepository;
    @Mock ArticleRepository articleRepository;
    @Mock WorldRepository worldRepository;
    @Mock StoryChainRepository storyChainRepository;
    @Mock GroupRepository groupRepository;
    @Mock CommentRepository commentRepository;

    @InjectMocks AdminServiceImpl adminService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adminService, "rootAdminId", 1L);
    }

    // ===== getStats =====

    @Test
    void getStats_includesComments() {
        when(userRepository.count()).thenReturn(10L);
        when(postRepository.count()).thenReturn(5L);
        when(articleRepository.count()).thenReturn(3L);
        when(worldRepository.count()).thenReturn(2L);
        when(storyChainRepository.count()).thenReturn(1L);
        when(groupRepository.count()).thenReturn(4L);
        when(commentRepository.count()).thenReturn(100L);

        Map<String, Long> stats = adminService.getStats();

        assertEquals(10L, stats.get("users"));
        assertEquals(5L, stats.get("posts"));
        assertEquals(100L, stats.get("comments"));
    }

    // ===== updateUserRole =====

    @Test
    void updateUserRole_rootCannotBeModified() {
        User rootUser = new User();
        rootUser.setId(1L); // same as rootAdminId
        rootUser.setRole(User.UserRole.SYS_ADMIN);

        when(userRepository.findById(1L)).thenReturn(Optional.of(rootUser));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> adminService.updateUserRole(1L, "USER", 1L));
        assertEquals(403, ex.getCode());
        assertTrue(ex.getMessage().contains("根管理员"));
    }

    @Test
    void updateUserRole_nonRootCannotModifySysAdmin() {
        User sysAdmin = new User();
        sysAdmin.setId(2L);
        sysAdmin.setRole(User.UserRole.SYS_ADMIN);

        when(userRepository.findById(2L)).thenReturn(Optional.of(sysAdmin));

        // operatorId=3 (not root)
        BusinessException ex = assertThrows(BusinessException.class,
                () -> adminService.updateUserRole(2L, "USER", 3L));
        assertEquals(403, ex.getCode());
        assertTrue(ex.getMessage().contains("只有根管理员"));
    }

    @Test
    void updateUserRole_nonRootCannotPromoteToSysAdmin() {
        User normalUser = new User();
        normalUser.setId(3L);
        normalUser.setRole(User.UserRole.USER);

        when(userRepository.findById(3L)).thenReturn(Optional.of(normalUser));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> adminService.updateUserRole(3L, "SYS_ADMIN", 3L));
        assertEquals(403, ex.getCode());
        assertTrue(ex.getMessage().contains("提权"));
    }

    @Test
    void updateUserRole_rootCanChangeAnyone() {
        User normalUser = new User();
        normalUser.setId(5L);
        normalUser.setRole(User.UserRole.USER);

        when(userRepository.findById(5L)).thenReturn(Optional.of(normalUser));

        adminService.updateUserRole(5L, "GROUP_ADMIN", 1L);

        assertEquals(User.UserRole.GROUP_ADMIN, normalUser.getRole());
        verify(userRepository).save(normalUser);
    }

    @Test
    void updateUserRole_invalidRole_throws400() {
        User user = new User();
        user.setId(5L);
        user.setRole(User.UserRole.USER);

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> adminService.updateUserRole(5L, "INVALID_ROLE", 1L));
        assertEquals(400, ex.getCode());
    }

    @Test
    void updateUserRole_userNotFound_throws404() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> adminService.updateUserRole(999L, "USER", 1L));
        assertEquals(404, ex.getCode());
    }

    // ===== publishArticle / unpublishArticle =====

    @Test
    void unpublishArticle_setsHiddenAndUnpublished() {
        Article article = new Article();
        article.setId(1L);
        article.setStatus("PUBLISHED");
        article.setIsPublished(true);

        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        adminService.unpublishArticle(1L);

        assertEquals("HIDDEN", article.getStatus());
        assertFalse(article.getIsPublished());
        verify(articleRepository).save(article);
    }

    @Test
    void publishArticle_setsPublished() {
        Article article = new Article();
        article.setId(1L);
        article.setStatus("HIDDEN");
        article.setIsPublished(false);

        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        adminService.publishArticle(1L);

        assertEquals("PUBLISHED", article.getStatus());
        assertTrue(article.getIsPublished());
        verify(articleRepository).save(article);
    }

    @Test
    void publishArticle_articleNotFound_throws404() {
        when(articleRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> adminService.publishArticle(999L));
        assertEquals(404, ex.getCode());
    }

    // ===== listUsers =====

    @Test
    void listUsers_filtersByRoleAndStatus() {
        User user1 = new User(); user1.setId(1L); user1.setUsername("a");
        user1.setRole(User.UserRole.USER); user1.setStatus(User.UserStatus.ACTIVE);
        User user2 = new User(); user2.setId(2L); user2.setUsername("b");
        user2.setRole(User.UserRole.SYS_ADMIN); user2.setStatus(User.UserStatus.ACTIVE);

        when(userRepository.findByUsernameContaining("a")).thenReturn(List.of(user1));

        List<UserVO> result = adminService.listUsers("a", null, null, 1, 20);
        assertEquals(1, result.size());
        assertEquals("USER", result.get(0).getRole());
        assertEquals("ACTIVE", result.get(0).getStatus());
    }
}
