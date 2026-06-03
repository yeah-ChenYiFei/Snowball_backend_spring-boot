package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.dto.ArticleCreateDTO;
import com.snowball.dto.ArticleUpdateDTO;
import com.snowball.entity.Article;
import com.snowball.entity.World;
import com.snowball.repository.ArticleRepository;
import com.snowball.repository.UserRepository;
import com.snowball.repository.WorldRepository;
import com.snowball.service.ArticleService;
import com.snowball.vo.ArticleVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final WorldRepository worldRepository;

    public ArticleServiceImpl(ArticleRepository articleRepository, UserRepository userRepository,
                              WorldRepository worldRepository) {
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.worldRepository = worldRepository;
    }

    @Override
    public List<ArticleVO> getUserArticles(Long userId, String type, String search) {
        List<Article> articles;

        boolean hasType = StringUtils.hasText(type);
        boolean hasSearch = StringUtils.hasText(search);

        if (hasType && hasSearch) {
            List<Article.ArticleType> types = Arrays.stream(type.split(","))
                    .map(Article.ArticleType::valueOf)
                    .toList();
            articles = articleRepository.searchByUserIdAndTypesAndTitle(userId, types, search, "DELETED");
        } else if (hasType) {
            List<Article.ArticleType> types = Arrays.stream(type.split(","))
                    .map(Article.ArticleType::valueOf)
                    .toList();
            articles = articleRepository.findByUserIdAndTypeInAndStatusNotAndWorldIdIsNullOrderByCreatedAtDesc(userId, types, "DELETED");
        } else if (hasSearch) {
            articles = articleRepository.searchByUserIdAndTitle(userId, search, "DELETED");
        } else {
            articles = articleRepository.findByUserIdAndStatusNotAndWorldIdIsNullOrderByCreatedAtDesc(userId, "DELETED");
        }

        return articles.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public ArticleVO getArticle(Long id, Long userId) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "文章不存在"));
        // Published articles are publicly viewable
        if (Boolean.TRUE.equals(article.getIsPublished())) {
            return toVO(article);
        }
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权查看他人文章");
        }
        return toVO(article);
    }

    @Override
    @Transactional
    public ArticleVO createArticle(Long userId, ArticleCreateDTO dto) {
        Article article = new Article();
        article.setUserId(userId);
        article.setType(Article.ArticleType.valueOf(dto.getType()));
        article.setTitle(dto.getTitle());
        article.setBody(dto.getBody());
        article.setChapter(dto.getChapter());
        return toVO(articleRepository.save(article));
    }

    @Override
    @Transactional
    public ArticleVO updateArticle(Long id, Long userId, ArticleUpdateDTO dto) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "文章不存在"));
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权编辑他人文章");
        }
        article.setTitle(dto.getTitle());
        article.setBody(dto.getBody());
        if (dto.getChapter() != null) {
            article.setChapter(dto.getChapter());
        }
        return toVO(articleRepository.save(article));
    }

    @Override
    @Transactional
    public void deleteArticle(Long id, Long userId) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "文章不存在"));
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除他人文章");
        }
        article.setStatus("DELETED");
        articleRepository.save(article);
    }

    @Override
    public int getDiaryStreak(Long userId) {
        List<Article> diaries = articleRepository.findByUserIdAndTypeAndStatusNotOrderByCreatedAtDesc(
                userId, Article.ArticleType.DIARY, "DELETED");
        if (diaries.isEmpty()) return 0;

        java.util.Set<java.time.LocalDate> diaryDates = diaries.stream()
                .map(a -> a.getCreatedAt().toLocalDate())
                .collect(Collectors.toSet());

        java.time.LocalDate today = java.time.LocalDate.now();
        int streak = 0;
        for (int i = 0; ; i++) {
            java.time.LocalDate date = today.minusDays(i);
            if (diaryDates.contains(date)) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }

    // ===== Published articles (文阁) =====

    @Override
    public List<ArticleVO> getPublishedArticles() {
        List<Article.ArticleType> types = List.of(Article.ArticleType.NOVEL, Article.ArticleType.ESSAY);
        return articleRepository.findByIsPublishedTrueAndTypeInAndWorldIdIsNullOrderByPublishedAtDesc(types)
                .stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ArticleVO publishArticle(Long id, Long userId) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "文章不存在"));
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }
        article.setIsPublished(true);
        article.setPublishedAt(LocalDateTime.now());
        return toVO(articleRepository.save(article));
    }

    @Override
    @Transactional
    public ArticleVO unpublishArticle(Long id, Long userId) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "文章不存在"));
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }
        article.setIsPublished(false);
        article.setPublishedAt(null);
        return toVO(articleRepository.save(article));
    }

    // ===== World binding =====

    @Override
    @Transactional
    public ArticleVO bindWorld(Long articleId, Long worldId, Long userId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new BusinessException(404, "文章不存在"));
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }
        article.setWorldId(worldId);
        return toVO(articleRepository.save(article));
    }

    @Override
    @Transactional
    public ArticleVO unbindWorld(Long articleId, Long userId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new BusinessException(404, "文章不存在"));
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }
        article.setWorldId(null);
        return toVO(articleRepository.save(article));
    }

    @Override
    public List<ArticleVO> getWorldBoundArticles(Long worldId, Long userId) {
        World world = worldRepository.findById(worldId)
                .orElseThrow(() -> new BusinessException(404, "世界不存在"));
        if (Boolean.FALSE.equals(world.getIsPublic()) && !world.getUserId().equals(userId)) {
            throw new BusinessException(403, "这个世界是私有的");
        }
        return articleRepository.findByWorldId(worldId).stream()
                .map(this::toVO).collect(Collectors.toList());
    }

    private ArticleVO toVO(Article a) {
        ArticleVO vo = new ArticleVO();
        vo.setId(a.getId());
        vo.setUserId(a.getUserId());
        vo.setType(a.getType().name());
        vo.setTitle(a.getTitle());
        vo.setBody(a.getBody());
        vo.setStatus(a.getStatus());
        vo.setChapter(a.getChapter());
        if (a.getBody() != null) {
            vo.setWordCount(a.getBody().length());
        }
        vo.setIsPublished(a.getIsPublished());
        vo.setPublishedAt(a.getPublishedAt());
        vo.setWorldId(a.getWorldId());
        if (a.getWorldId() != null) {
            worldRepository.findById(a.getWorldId()).ifPresent(w -> vo.setWorldName(w.getName()));
        }
        vo.setCreatedAt(a.getCreatedAt());
        vo.setUpdatedAt(a.getUpdatedAt());
        userRepository.findById(a.getUserId()).ifPresent(u -> vo.setAuthorName(u.getUsername()));
        return vo;
    }
}
