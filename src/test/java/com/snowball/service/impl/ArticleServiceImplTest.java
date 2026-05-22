package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.entity.World;
import com.snowball.repository.ArticleRepository;
import com.snowball.repository.UserRepository;
import com.snowball.repository.WorldRepository;
import com.snowball.vo.ArticleVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceImplTest {

    @Mock ArticleRepository articleRepository;
    @Mock WorldRepository worldRepository;

    @InjectMocks ArticleServiceImpl articleService;

    // ===== getWorldBoundArticles =====

    @Test
    void getWorldBoundArticles_worldNotFound_throws404() {
        when(worldRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> articleService.getWorldBoundArticles(999L, 1L));
        assertEquals(404, ex.getCode());
        assertTrue(ex.getMessage().contains("世界不存在"));
    }

    @Test
    void getWorldBoundArticles_privateWorld_nonOwner_throws403() {
        World privateWorld = new World();
        privateWorld.setId(1L);
        privateWorld.setUserId(10L);
        privateWorld.setIsPublic(false);

        when(worldRepository.findById(1L)).thenReturn(Optional.of(privateWorld));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> articleService.getWorldBoundArticles(1L, 99L)); // userId 99 is not owner
        assertEquals(403, ex.getCode());
        assertTrue(ex.getMessage().contains("私有"));
    }

    @Test
    void getWorldBoundArticles_privateWorld_owner_succeeds() {
        World privateWorld = new World();
        privateWorld.setId(1L);
        privateWorld.setUserId(10L);
        privateWorld.setIsPublic(false);

        when(worldRepository.findById(1L)).thenReturn(Optional.of(privateWorld));
        when(articleRepository.findByWorldId(1L)).thenReturn(List.of());

        List<ArticleVO> result = articleService.getWorldBoundArticles(1L, 10L);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getWorldBoundArticles_publicWorld_anonymous_succeeds() {
        World publicWorld = new World();
        publicWorld.setId(1L);
        publicWorld.setUserId(10L);
        publicWorld.setIsPublic(true);

        when(worldRepository.findById(1L)).thenReturn(Optional.of(publicWorld));
        when(articleRepository.findByWorldId(1L)).thenReturn(List.of());

        List<ArticleVO> result = articleService.getWorldBoundArticles(1L, null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
