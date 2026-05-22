package com.snowball.service;

import com.snowball.dto.*;
import com.snowball.vo.*;

import java.util.List;

public interface NovelService {
    List<NovelVO> getUserNovels(Long userId, String search);
    NovelDetailVO getNovelDetail(Long id, Long userId);
    NovelVO createNovel(Long userId, NovelCreateDTO dto);
    NovelVO updateNovel(Long id, Long userId, NovelUpdateDTO dto);
    void deleteNovel(Long id, Long userId);

    NovelChapterVO saveChapter(Long novelId, Long userId, NovelChapterCreateDTO dto);
    NovelChapterVO updateChapter(Long chapterId, Long userId, NovelChapterUpdateDTO dto);
    void deleteChapter(Long chapterId, Long userId);

    List<NovelVO> getPublishedNovels();
    NovelVO publishNovel(Long id, Long userId);
    NovelVO unpublishNovel(Long id, Long userId);
    NovelVO bindWorld(Long novelId, Long worldId, Long userId);
    NovelVO unbindWorld(Long novelId, Long userId);
    List<NovelVO> getWorldBoundNovels(Long worldId, Long userId);
}
