package com.snowball.service.impl;

import com.snowball.common.BusinessException;
import com.snowball.dto.*;
import com.snowball.entity.Novel;
import com.snowball.entity.NovelChapter;
import com.snowball.entity.World;
import com.snowball.repository.NovelChapterRepository;
import com.snowball.repository.NovelRepository;
import com.snowball.repository.UserRepository;
import com.snowball.repository.WorldRepository;
import com.snowball.service.NovelService;
import com.snowball.vo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NovelServiceImpl implements NovelService {

    private final NovelRepository novelRepository;
    private final NovelChapterRepository chapterRepository;
    private final UserRepository userRepository;
    private final WorldRepository worldRepository;

    public NovelServiceImpl(NovelRepository novelRepository,
                            NovelChapterRepository chapterRepository,
                            UserRepository userRepository,
                            WorldRepository worldRepository) {
        this.novelRepository = novelRepository;
        this.chapterRepository = chapterRepository;
        this.userRepository = userRepository;
        this.worldRepository = worldRepository;
    }

    @Override
    public List<NovelVO> getUserNovels(Long userId, String search) {
        List<Novel> novels;
        if (StringUtils.hasText(search)) {
            novels = novelRepository.searchByUserIdAndTitle(userId, search, "DELETED");
        } else {
            novels = novelRepository.findByUserIdAndStatusNotOrderByUpdatedAtDesc(userId, "DELETED");
        }
        return novels.stream().map(this::toNovelVO).collect(Collectors.toList());
    }

    @Override
    public NovelDetailVO getNovelDetail(Long id, Long userId) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "小说不存在"));
        if (Boolean.TRUE.equals(novel.getIsPublished())) {
            return toNovelDetailVO(novel);
        }
        if (!novel.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权查看他人小说");
        }
        return toNovelDetailVO(novel);
    }

    @Override
    @Transactional
    public NovelVO createNovel(Long userId, NovelCreateDTO dto) {
        Novel novel = new Novel();
        novel.setUserId(userId);
        novel.setTitle(dto.getTitle());
        novel.setDescription(dto.getDescription());
        novel.setHasVolumes(dto.getHasVolumes() != null && dto.getHasVolumes());
        novel.setWorldId(dto.getWorldId());
        return toNovelVO(novelRepository.save(novel));
    }

    @Override
    @Transactional
    public NovelVO updateNovel(Long id, Long userId, NovelUpdateDTO dto) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "小说不存在"));
        if (!novel.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权编辑他人小说");
        }
        novel.setTitle(dto.getTitle());
        if (dto.getDescription() != null) {
            novel.setDescription(dto.getDescription());
        }
        if (dto.getHasVolumes() != null) {
            novel.setHasVolumes(dto.getHasVolumes());
        }
        return toNovelVO(novelRepository.save(novel));
    }

    @Override
    @Transactional
    public void deleteNovel(Long id, Long userId) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "小说不存在"));
        if (!novel.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除他人小说");
        }
        novel.setStatus("DELETED");
        novelRepository.save(novel);
    }

    // ===== Chapter management =====

    @Override
    @Transactional
    public NovelChapterVO saveChapter(Long novelId, Long userId, NovelChapterCreateDTO dto) {
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new BusinessException(404, "小说不存在"));
        if (!novel.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权编辑");
        }

        String section = dto.getSection() != null ? dto.getSection() : "main";
        int volume = dto.getVolumeNumber() != null ? dto.getVolumeNumber() : 0;
        int chapterNum = dto.getChapterNumber() != null ? dto.getChapterNumber() : 1;

        NovelChapter chapter = chapterRepository
                .findByNovelIdAndSectionAndVolumeNumberAndChapterNumber(novelId, section, volume, chapterNum)
                .orElseGet(NovelChapter::new);

        chapter.setNovelId(novelId);
        chapter.setSection(section);
        chapter.setVolumeNumber(volume);
        chapter.setChapterNumber(chapterNum);
        chapter.setTitle(dto.getTitle());
        chapter.setBody(dto.getBody() != null ? dto.getBody() : "");
        if (dto.getBody() != null) {
            chapter.setWordCount(dto.getBody().length());
        }

        NovelChapter saved = chapterRepository.save(chapter);
        novelRepository.save(novel); // updates updatedAt
        return toChapterVO(saved);
    }

    @Override
    @Transactional
    public NovelChapterVO updateChapter(Long chapterId, Long userId, NovelChapterUpdateDTO dto) {
        NovelChapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new BusinessException(404, "章节不存在"));
        Novel novel = novelRepository.findById(chapter.getNovelId())
                .orElseThrow(() -> new BusinessException(404, "小说不存在"));
        if (!novel.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权编辑");
        }
        if (dto.getTitle() != null) {
            chapter.setTitle(dto.getTitle());
        }
        if (dto.getBody() != null) {
            chapter.setBody(dto.getBody());
            chapter.setWordCount(dto.getBody().length());
        }
        NovelChapter saved = chapterRepository.save(chapter);
        novelRepository.save(novel);
        return toChapterVO(saved);
    }

    @Override
    @Transactional
    public void deleteChapter(Long chapterId, Long userId) {
        NovelChapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new BusinessException(404, "章节不存在"));
        Novel novel = novelRepository.findById(chapter.getNovelId())
                .orElseThrow(() -> new BusinessException(404, "小说不存在"));
        if (!novel.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权删除");
        }
        chapterRepository.delete(chapter);
        novelRepository.save(novel);
    }

    // ===== Published novels =====

    @Override
    public List<NovelVO> getPublishedNovels() {
        return novelRepository.findByIsPublishedTrueOrderByPublishedAtDesc()
                .stream().map(this::toNovelVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NovelVO publishNovel(Long id, Long userId) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "小说不存在"));
        if (!novel.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }
        novel.setIsPublished(true);
        novel.setPublishedAt(LocalDateTime.now());
        return toNovelVO(novelRepository.save(novel));
    }

    @Override
    @Transactional
    public NovelVO unpublishNovel(Long id, Long userId) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "小说不存在"));
        if (!novel.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }
        novel.setIsPublished(false);
        novel.setPublishedAt(null);
        return toNovelVO(novelRepository.save(novel));
    }

    // ===== World binding =====

    @Override
    @Transactional
    public NovelVO bindWorld(Long novelId, Long worldId, Long userId) {
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new BusinessException(404, "小说不存在"));
        if (!novel.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }
        novel.setWorldId(worldId);
        return toNovelVO(novelRepository.save(novel));
    }

    @Override
    @Transactional
    public NovelVO unbindWorld(Long novelId, Long userId) {
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new BusinessException(404, "小说不存在"));
        if (!novel.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作");
        }
        novel.setWorldId(null);
        return toNovelVO(novelRepository.save(novel));
    }

    @Override
    public List<NovelVO> getWorldBoundNovels(Long worldId, Long userId) {
        World world = worldRepository.findById(worldId)
                .orElseThrow(() -> new BusinessException(404, "世界不存在"));
        if (Boolean.FALSE.equals(world.getIsPublic()) && !world.getUserId().equals(userId)) {
            throw new BusinessException(403, "这个世界是私有的");
        }
        return novelRepository.findByWorldId(worldId).stream()
                .map(this::toNovelVO).collect(Collectors.toList());
    }

    // ===== Mapping helpers =====

    private NovelVO toNovelVO(Novel n) {
        NovelVO vo = new NovelVO();
        vo.setId(n.getId());
        vo.setUserId(n.getUserId());
        vo.setTitle(n.getTitle());
        vo.setDescription(n.getDescription());
        vo.setHasVolumes(n.getHasVolumes());
        vo.setWorldId(n.getWorldId());
        if (n.getWorldId() != null) {
            worldRepository.findById(n.getWorldId()).ifPresent(w -> vo.setWorldName(w.getName()));
        }
        vo.setIsPublished(n.getIsPublished());
        vo.setPublishedAt(n.getPublishedAt());
        vo.setChapterCount(chapterRepository.countByNovelId(n.getId()));
        vo.setTotalWordCount(chapterRepository.getTotalWordCount(n.getId()));
        vo.setCreatedAt(n.getCreatedAt());
        vo.setUpdatedAt(n.getUpdatedAt());
        userRepository.findById(n.getUserId()).ifPresent(u -> vo.setAuthorName(u.getUsername()));
        return vo;
    }

    private NovelDetailVO toNovelDetailVO(Novel n) {
        NovelDetailVO vo = new NovelDetailVO();
        vo.setId(n.getId());
        vo.setUserId(n.getUserId());
        vo.setTitle(n.getTitle());
        vo.setDescription(n.getDescription());
        vo.setHasVolumes(n.getHasVolumes());
        vo.setWorldId(n.getWorldId());
        if (n.getWorldId() != null) {
            worldRepository.findById(n.getWorldId()).ifPresent(w -> vo.setWorldName(w.getName()));
        }
        vo.setIsPublished(n.getIsPublished());
        vo.setPublishedAt(n.getPublishedAt());
        vo.setChapterCount(chapterRepository.countByNovelId(n.getId()));
        vo.setTotalWordCount(chapterRepository.getTotalWordCount(n.getId()));
        vo.setCreatedAt(n.getCreatedAt());
        vo.setUpdatedAt(n.getUpdatedAt());
        userRepository.findById(n.getUserId()).ifPresent(u -> vo.setAuthorName(u.getUsername()));

        List<NovelChapterVO> chapters = chapterRepository
                .findByNovelIdOrderBySectionAscVolumeNumberAscChapterNumberAsc(n.getId())
                .stream().map(this::toChapterVO).collect(Collectors.toList());
        vo.setChapters(chapters);
        return vo;
    }

    private NovelChapterVO toChapterVO(NovelChapter c) {
        NovelChapterVO vo = new NovelChapterVO();
        vo.setId(c.getId());
        vo.setNovelId(c.getNovelId());
        vo.setSection(c.getSection());
        vo.setVolumeNumber(c.getVolumeNumber());
        vo.setChapterNumber(c.getChapterNumber());
        vo.setTitle(c.getTitle());
        vo.setBody(c.getBody());
        vo.setWordCount(c.getWordCount());
        vo.setCreatedAt(c.getCreatedAt());
        vo.setUpdatedAt(c.getUpdatedAt());
        return vo;
    }
}
