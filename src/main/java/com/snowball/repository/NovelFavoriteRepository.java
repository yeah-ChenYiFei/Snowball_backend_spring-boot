package com.snowball.repository;

import com.snowball.entity.NovelFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NovelFavoriteRepository extends JpaRepository<NovelFavorite, Long> {
    Optional<NovelFavorite> findByUserIdAndNovelId(Long userId, Long novelId);
    List<NovelFavorite> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<NovelFavorite> findByUserIdAndNovelIdIn(Long userId, List<Long> novelIds);
    boolean existsByUserIdAndNovelId(Long userId, Long novelId);
    long countByNovelId(Long novelId);
}
