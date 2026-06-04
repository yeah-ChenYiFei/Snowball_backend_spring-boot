package com.snowball.controller;

import com.snowball.common.Result;
import com.snowball.dto.*;
import com.snowball.service.NovelService;
import com.snowball.vo.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/novels")
public class NovelController extends BaseController {

    private final NovelService novelService;

    public NovelController(NovelService novelService) {
        this.novelService = novelService;
    }

    @GetMapping
    public Result<List<NovelVO>> getUserNovels(@RequestParam(required = false) String search) {
        return Result.success(novelService.getUserNovels(getCurrentUserId(), search));
    }

    @GetMapping("/published")
    public Result<List<NovelVO>> getPublishedNovels() {
        return Result.success(novelService.getPublishedNovels());
    }

    @GetMapping("/{id}")
    public Result<NovelDetailVO> getNovelDetail(@PathVariable Long id) {
        return Result.success(novelService.getNovelDetail(id, getOptionalUserId()));
    }

    @PostMapping
    public Result<NovelVO> createNovel(@Valid @RequestBody NovelCreateDTO dto) {
        return Result.success(novelService.createNovel(getCurrentUserId(), dto));
    }

    @PutMapping("/{id}")
    public Result<NovelVO> updateNovel(@PathVariable Long id, @Valid @RequestBody NovelUpdateDTO dto) {
        return Result.success(novelService.updateNovel(id, getCurrentUserId(), dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteNovel(@PathVariable Long id) {
        novelService.deleteNovel(id, getCurrentUserId());
        return Result.success();
    }

    @PostMapping("/{id}/publish")
    public Result<NovelVO> publishNovel(@PathVariable Long id) {
        return Result.success(novelService.publishNovel(id, getCurrentUserId()));
    }

    @PostMapping("/{id}/unpublish")
    public Result<NovelVO> unpublishNovel(@PathVariable Long id) {
        return Result.success(novelService.unpublishNovel(id, getCurrentUserId()));
    }

    @PutMapping("/{id}/bind-world")
    public Result<NovelVO> bindWorld(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        return Result.success(novelService.bindWorld(id, body.get("worldId"), getCurrentUserId()));
    }

    @DeleteMapping("/{id}/bind-world")
    public Result<NovelVO> unbindWorld(@PathVariable Long id) {
        return Result.success(novelService.unbindWorld(id, getCurrentUserId()));
    }

    @GetMapping("/by-world/{worldId}")
    public Result<List<NovelVO>> getWorldBoundNovels(@PathVariable Long worldId) {
        return Result.success(novelService.getWorldBoundNovels(worldId, getOptionalUserId()));
    }

    // ===== Chapter endpoints =====

    @PostMapping("/{novelId}/chapters")
    public Result<NovelChapterVO> saveChapter(@PathVariable Long novelId, @RequestBody NovelChapterCreateDTO dto) {
        return Result.success(novelService.saveChapter(novelId, getCurrentUserId(), dto));
    }

    @PutMapping("/chapters/{chapterId}")
    public Result<NovelChapterVO> updateChapter(@PathVariable Long chapterId, @RequestBody NovelChapterUpdateDTO dto) {
        return Result.success(novelService.updateChapter(chapterId, getCurrentUserId(), dto));
    }

    @DeleteMapping("/chapters/{chapterId}")
    public Result<Void> deleteChapter(@PathVariable Long chapterId) {
        novelService.deleteChapter(chapterId, getCurrentUserId());
        return Result.success();
    }

    @PostMapping("/{id}/favorite")
    public Result<Boolean> toggleFavorite(@PathVariable Long id) {
        return Result.success(novelService.toggleFavorite(id, getCurrentUserId()));
    }

    @GetMapping("/favorites")
    public Result<List<NovelVO>> getFavorites() {
        return Result.success(novelService.getFavoriteNovels(getCurrentUserId()));
    }

    @GetMapping("/{id}/favorite/status")
    public Result<Boolean> checkFavoriteStatus(@PathVariable Long id) {
        return Result.success(novelService.checkFavoriteStatus(id, getCurrentUserId()));
    }
}
