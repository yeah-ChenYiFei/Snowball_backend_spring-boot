package com.snowball.repository;

import com.snowball.entity.NovelChapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NovelChapterRepository extends JpaRepository<NovelChapter, Long> {

    List<NovelChapter> findByNovelIdOrderBySectionAscVolumeNumberAscChapterNumberAsc(Long novelId);

    Optional<NovelChapter> findByNovelIdAndSectionAndVolumeNumberAndChapterNumber(
            Long novelId, String section, Integer volumeNumber, Integer chapterNumber);

    @Query("SELECT COALESCE(SUM(c.wordCount), 0) FROM NovelChapter c WHERE c.novelId = :novelId")
    int getTotalWordCount(@Param("novelId") Long novelId);

    @Query("SELECT COALESCE(COUNT(c), 0) FROM NovelChapter c WHERE c.novelId = :novelId")
    int countByNovelId(@Param("novelId") Long novelId);
}
