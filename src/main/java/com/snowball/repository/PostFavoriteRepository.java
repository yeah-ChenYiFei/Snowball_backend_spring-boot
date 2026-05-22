package com.snowball.repository;

import com.snowball.entity.PostFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostFavoriteRepository extends JpaRepository<PostFavorite, Long> {
    Optional<PostFavorite> findByUserIdAndPostId(Long userId, Long postId);
    List<PostFavorite> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<PostFavorite> findByUserIdAndPostIdIn(Long userId, List<Long> postIds);
    boolean existsByUserIdAndPostId(Long userId, Long postId);
    long countByPostId(Long postId);
}
