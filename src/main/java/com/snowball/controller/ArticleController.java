package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.dto.ArticleCreateDTO;
import com.snowball.dto.ArticleUpdateDTO;
import com.snowball.service.ArticleService;
import com.snowball.vo.ArticleVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/articles")
public class ArticleController extends BaseController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public Result<List<ArticleVO>> getUserArticles(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search) {
        return Result.success(articleService.getUserArticles(getCurrentUserId(), type, search));
    }

    @GetMapping("/{id}")
    public Result<ArticleVO> getArticle(@PathVariable Long id) {
        return Result.success(articleService.getArticle(id, getOptionalUserId()));
    }

    @GetMapping("/published")
    public Result<List<ArticleVO>> getPublishedArticles() {
        return Result.success(articleService.getPublishedArticles());
    }

    @PostMapping("/{id}/publish")
    public Result<ArticleVO> publishArticle(@PathVariable Long id) {
        return Result.success(articleService.publishArticle(id, getCurrentUserId()));
    }

    @PostMapping("/{id}/unpublish")
    public Result<ArticleVO> unpublishArticle(@PathVariable Long id) {
        return Result.success(articleService.unpublishArticle(id, getCurrentUserId()));
    }

    @PutMapping("/{id}/bind-world")
    public Result<ArticleVO> bindWorld(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        return Result.success(articleService.bindWorld(id, body.get("worldId"), getCurrentUserId()));
    }

    @DeleteMapping("/{id}/bind-world")
    public Result<ArticleVO> unbindWorld(@PathVariable Long id) {
        return Result.success(articleService.unbindWorld(id, getCurrentUserId()));
    }

    @PostMapping
    public Result<ArticleVO> createArticle(@Valid @RequestBody ArticleCreateDTO dto) {
        return Result.success(articleService.createArticle(getCurrentUserId(), dto));
    }

    @PutMapping("/{id}")
    public Result<ArticleVO> updateArticle(@PathVariable Long id, @Valid @RequestBody ArticleUpdateDTO dto) {
        return Result.success(articleService.updateArticle(id, getCurrentUserId(), dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id, getCurrentUserId());
        return Result.success();
    }

    @GetMapping("/diary-streak")
    public Result<Integer> getDiaryStreak() {
        return Result.success(articleService.getDiaryStreak(getCurrentUserId()));
    }

    @GetMapping("/by-world/{worldId}")
    public Result<List<ArticleVO>> getWorldBoundArticles(@PathVariable Long worldId) {
        return Result.success(articleService.getWorldBoundArticles(worldId));
    }
}
