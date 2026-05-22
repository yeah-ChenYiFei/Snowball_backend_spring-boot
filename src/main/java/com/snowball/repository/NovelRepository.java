package com.snowball.repository;

import com.snowball.entity.Novel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NovelRepository extends JpaRepository<Novel, Long> {

    List<Novel> findByUserIdAndStatusNotOrderByUpdatedAtDesc(Long userId, String status);

    @Query("SELECT n FROM Novel n WHERE n.userId = :userId AND n.status <> :status AND n.title LIKE %:keyword% ORDER BY n.updatedAt DESC")
    List<Novel> searchByUserIdAndTitle(@Param("userId") Long userId, @Param("keyword") String keyword, @Param("status") String status);

    List<Novel> findByIsPublishedTrueOrderByPublishedAtDesc();

    List<Novel> findByWorldId(Long worldId);
}
