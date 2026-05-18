package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.dto.ArticleCreateDTO;
import com.snowball.dto.ArticleUpdateDTO;
import com.snowball.service.ArticleService;
import com.snowball.vo.ArticleVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return Result.success(articleService.getArticle(id, getCurrentUserId()));
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
}
