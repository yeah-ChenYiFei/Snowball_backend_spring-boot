package com.snowball.repository;

import com.snowball.entity.Post;
import com.snowball.entity.Post.PostType;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Post> findByUserIdAndStatusNotInOrderByCreatedAtDesc(Long userId, List<String> excludedStatuses);

    // Paginated query for public posts (prevents loading all rows into memory)
    Page<Post> findByStatusNotIn(List<String> excludedStatuses, Pageable pageable);

    // Non-paginated (existing, kept for backward compat)
    List<Post> findByStatusNotIn(List<String> excludedStatuses);

    List<Post> findByTitleContainingIgnoreCaseAndStatusNotIn(String title, List<String> excludedStatuses);

    List<Post> findByTypeAndStatusNotIn(PostType type, List<String> excludedStatuses);

    /**
     * Atomic view count increment — avoids the read-then-write race condition
     * that would lose concurrent view increments under @Transactional.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Post p SET p.viewCount = COALESCE(p.viewCount, 0) + 1 WHERE p.id = :id")
    void incrementViewCount(Long id);
}
