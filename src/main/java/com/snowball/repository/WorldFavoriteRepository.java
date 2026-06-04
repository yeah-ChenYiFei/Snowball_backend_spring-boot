package com.snowball.repository;

import com.snowball.entity.WorldFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorldFavoriteRepository extends JpaRepository<WorldFavorite, Long> {
    Optional<WorldFavorite> findByUserIdAndWorldId(Long userId, Long worldId);
    List<WorldFavorite> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<WorldFavorite> findByUserIdAndWorldIdIn(Long userId, List<Long> worldIds);
    boolean existsByUserIdAndWorldId(Long userId, Long worldId);
    long countByWorldId(Long worldId);
}
