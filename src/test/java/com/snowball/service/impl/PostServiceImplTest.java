package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.entity.Post;
import com.snowball.entity.PostFavorite;
import com.snowball.entity.PostReaction;
import com.snowball.entity.User;
import com.snowball.repository.*;
import com.snowball.service.NotificationService;
import com.snowball.vo.PostDetailVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock PostRepository postRepository;
    @Mock PostVersionRepository postVersionRepository;
    @Mock UserRepository userRepository;
    @Mock CommentRepository commentRepository;
    @Mock PostReactionRepository postReactionRepository;
    @Mock PostTagRepository postTagRepository;
    @Mock TagRepository tagRepository;
    @Mock PostFavoriteRepository postFavoriteRepository;
    @Mock NotificationService notificationService;

    @InjectMocks PostServiceImpl postService;

    private Post post1, post2, post3, hiddenPost;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("alice");

        post1 = new Post();
        post1.setId(1L);
        post1.setUserId(1L);
        post1.setType(Post.PostType.THOUGHT);
        post1.setTitle("First");
        post1.setCurrentBody("Body 1");
        post1.setStatus("PUBLIC");
        post1.setCreatedAt(LocalDateTime.of(2026, 1, 1, 12, 0));
        post1.setViewCount(0L);

        post2 = new Post();
        post2.setId(2L);
        post2.setUserId(1L);
        post2.setType(Post.PostType.THOUGHT);
        post2.setTitle("Second");
        post2.setCurrentBody("Body 2");
        post2.setStatus("PUBLIC");
        post2.setCreatedAt(LocalDateTime.of(2026, 1, 2, 12, 0));
        post2.setViewCount(0L);

        post3 = new Post();
        post3.setId(3L);
        post3.setUserId(2L);
        post3.setType(Post.PostType.THOUGHT);
        post3.setTitle("Third");
        post3.setCurrentBody("Body 3");
        post3.setStatus("PUBLIC");
        post3.setCreatedAt(LocalDateTime.of(2026, 1, 3, 12, 0));
        post3.setViewCount(0L);

        hiddenPost = new Post();
        hiddenPost.setId(99L);
        hiddenPost.setUserId(1L);
        hiddenPost.setType(Post.PostType.THOUGHT);
        hiddenPost.setTitle("Hidden");
        hiddenPost.setCurrentBody("Secret");
        hiddenPost.setStatus("HIDDEN");
        hiddenPost.setCreatedAt(LocalDateTime.of(2026, 5, 1, 12, 0));
        hiddenPost.setViewCount(0L);
    }

    // ===== getAllPosts =====

    @Test
    void getAllPosts_filtersHiddenAndDeleted() {
        when(postRepository.findByStatusNotIn(argThat(list ->
                list.contains("HIDDEN") && list.contains("DELETED"))))
                .thenReturn(List.of(post1, post2));

        when(userRepository.findAllById(anyList())).thenReturn(List.of(user));
        when(commentRepository.countByPostIdIn(anyList())).thenReturn(List.of());
        when(postTagRepository.findByPostIdIn(anyList())).thenReturn(List.of());
        when(postReactionRepository.countGroupByPostIds(anyList())).thenReturn(List.of());
        when(postFavoriteRepository.findByUserIdAndPostIdIn(any(), anyList())).thenReturn(List.of());

        List<PostDetailVO> result = postService.getAllPosts(1L, "new");

        assertEquals(2, result.size());
    }

    @Test
    void getAllPosts_sortByNew() {
        when(postRepository.findByStatusNotIn(argThat(list ->
                list.contains("HIDDEN") && list.contains("DELETED"))))
                .thenReturn(List.of(post1, post2, post3));

        when(userRepository.findAllById(anyList())).thenReturn(List.of(user));
        User user2 = new User(); user2.setId(2L); user2.setUsername("bob");
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));

        when(commentRepository.countByPostIdIn(anyList())).thenReturn(List.of());
        when(postTagRepository.findByPostIdIn(anyList())).thenReturn(List.of());
        when(postReactionRepository.countGroupByPostIds(anyList())).thenReturn(List.of());
        when(postFavoriteRepository.findByUserIdAndPostIdIn(any(), anyList())).thenReturn(List.of());

        List<PostDetailVO> result = postService.getAllPosts(1L, "new");

        assertEquals(3, result.size());
        // newest first
        assertTrue(result.get(0).getCreatedAt().isAfter(result.get(1).getCreatedAt()));
        assertTrue(result.get(1).getCreatedAt().isAfter(result.get(2).getCreatedAt()));
    }

    @Test
    void getAllPosts_sortByTop() {
        post1.setViewCount(0L); // likeCount = 0 by default
        post2.setViewCount(0L); // likeCount = 0
        post3.setViewCount(0L); // likeCount = 0

        when(postRepository.findByStatusNotIn(argThat(list ->
                list.contains("HIDDEN") && list.contains("DELETED"))))
                .thenReturn(List.of(post1, post2, post3));

        when(userRepository.findAllById(anyList())).thenReturn(List.of(user));
        User user2 = new User(); user2.setId(2L); user2.setUsername("bob");
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));

        when(commentRepository.countByPostIdIn(anyList())).thenReturn(List.of());
        when(postTagRepository.findByPostIdIn(anyList())).thenReturn(List.of());
        when(postFavoriteRepository.findByUserIdAndPostIdIn(any(), anyList())).thenReturn(List.of());

        // post1 has 5 likes, post2 has 1 like, post3 has 10 likes
        when(postReactionRepository.countGroupByPostIds(anyList())).thenReturn(List.of(
                new Object[]{1L, PostReaction.ReactionType.LIKE, 5L},
                new Object[]{2L, PostReaction.ReactionType.LIKE, 1L},
                new Object[]{3L, PostReaction.ReactionType.LIKE, 10L}
        ));

        List<PostDetailVO> result = postService.getAllPosts(1L, "top");

        assertEquals(3, result.size());
        assertEquals(10L, result.get(0).getLikeCount());
        assertEquals(5L, result.get(1).getLikeCount());
        assertEquals(1L, result.get(2).getLikeCount());
    }

    // ===== getPostById =====

    @Test
    void getPostById_hiddenPost_nonOwner_throws404() {
        when(postRepository.findById(99L)).thenReturn(Optional.of(hiddenPost));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> postService.getPostById(99L, 999L));
        assertEquals(404, ex.getCode());
    }

    @Test
    void getPostById_hiddenPost_owner_returnsPost() {
        when(postRepository.findById(99L)).thenReturn(Optional.of(hiddenPost));
        when(commentRepository.countByPostIdAndIsDeletedFalse(99L)).thenReturn(0);
        when(postFavoriteRepository.existsByUserIdAndPostId(1L, 99L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postTagRepository.findByPostId(99L)).thenReturn(List.of());

        PostDetailVO result = postService.getPostById(99L, 1L);

        assertNotNull(result);
        assertEquals(99L, result.getId());
    }

    @Test
    void getPostById_notFound_throws404() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> postService.getPostById(999L, 1L));
        assertEquals(404, ex.getCode());
    }

    // ===== toggleFavorite =====

    @Test
    void toggleFavorite_create_returnsTrue() {
        when(postFavoriteRepository.findByUserIdAndPostId(1L, 1L)).thenReturn(Optional.empty());

        boolean result = postService.toggleFavorite(1L, 1L);

        assertTrue(result);
        verify(postFavoriteRepository).save(any(PostFavorite.class));
    }

    @Test
    void toggleFavorite_delete_returnsFalse() {
        PostFavorite existing = new PostFavorite();
        existing.setUserId(1L);
        existing.setPostId(1L);
        when(postFavoriteRepository.findByUserIdAndPostId(1L, 1L)).thenReturn(Optional.of(existing));

        boolean result = postService.toggleFavorite(1L, 1L);

        assertFalse(result);
        verify(postFavoriteRepository).delete(existing);
    }

    // ===== getFavoritePosts =====

    @Test
    void getFavoritePosts_returnsWithIsFavoritedTrue() {
        PostFavorite fav = new PostFavorite();
        fav.setUserId(1L);
        fav.setPostId(1L);

        when(postFavoriteRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(fav));
        when(postRepository.findAllById(List.of(1L))).thenReturn(List.of(post1));
        when(userRepository.findAllById(anyList())).thenReturn(List.of(user));
        when(commentRepository.countByPostIdIn(anyList())).thenReturn(List.of());
        when(postTagRepository.findByPostIdIn(anyList())).thenReturn(List.of());

        List<PostDetailVO> result = postService.getFavoritePosts(1L);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsFavorited());
    }

    @Test
    void getFavoritePosts_empty_returnsEmptyList() {
        when(postFavoriteRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        List<PostDetailVO> result = postService.getFavoritePosts(1L);

        assertTrue(result.isEmpty());
    }

    // ===== forceDeletePost =====

    @Test
    void forceDeletePost_cleansRelatedData() {
        postService.forceDeletePost(1L);

        verify(commentRepository).deleteByPostId(1L);
        verify(postReactionRepository).deleteByPostId(1L);
        verify(postTagRepository).deleteByPostId(1L);
        verify(postVersionRepository).deleteByPostId(1L);
        verify(postRepository).deleteById(1L);
    }
}
