package com.snowball.service;

import com.snowball.dto.ArticleCreateDTO;
import com.snowball.dto.ArticleUpdateDTO;
import com.snowball.vo.ArticleVO;

import java.util.List;

public interface ArticleService {
    List<ArticleVO> getUserArticles(Long userId, String type, String search);
    ArticleVO getArticle(Long id, Long userId);
    ArticleVO createArticle(Long userId, ArticleCreateDTO dto);
    ArticleVO updateArticle(Long id, Long userId, ArticleUpdateDTO dto);
    void deleteArticle(Long id, Long userId);
    int getDiaryStreak(Long userId);

    List<ArticleVO> getPublishedArticles();
    ArticleVO publishArticle(Long id, Long userId);
    ArticleVO unpublishArticle(Long id, Long userId);
    ArticleVO bindWorld(Long articleId, Long worldId, Long userId);
    ArticleVO unbindWorld(Long articleId, Long userId);
    List<ArticleVO> getWorldBoundArticles(Long worldId, Long userId);
}
