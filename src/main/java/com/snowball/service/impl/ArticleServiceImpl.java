package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.dto.ArticleCreateDTO;
import com.snowball.dto.ArticleUpdateDTO;
import com.snowball.entity.Article;
import com.snowball.repository.ArticleRepository;
import com.snowball.repository.UserRepository;
import com.snowball.service.ArticleService;
import com.snowball.vo.ArticleVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    public ArticleServiceImpl(ArticleRepository articleRepository, UserRepository userRepository) {
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
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
            articles = articleRepository.findByUserIdAndTypeInAndStatusNotOrderByCreatedAtDesc(userId, types, "DELETED");
        } else if (hasSearch) {
            articles = articleRepository.searchByUserIdAndTitle(userId, search, "DELETED");
        } else {
            articles = articleRepository.findByUserIdAndStatusNotOrderByCreatedAtDesc(userId, "DELETED");
        }

        return articles.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public ArticleVO getArticle(Long id, Long userId) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "文章不存在"));
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
        vo.setCreatedAt(a.getCreatedAt());
        vo.setUpdatedAt(a.getUpdatedAt());
        userRepository.findById(a.getUserId()).ifPresent(u -> vo.setAuthorName(u.getUsername()));
        return vo;
    }
}
