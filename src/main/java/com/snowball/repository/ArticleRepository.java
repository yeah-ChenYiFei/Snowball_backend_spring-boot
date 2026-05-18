package com.snowball.repository;

import com.snowball.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    List<Article> findByUserIdAndStatusNotOrderByCreatedAtDesc(Long userId, String status);

    List<Article> findByUserIdAndTypeAndStatusNotOrderByCreatedAtDesc(Long userId, Article.ArticleType type, String status);

    List<Article> findByUserIdAndTypeInAndStatusNotOrderByCreatedAtDesc(Long userId, List<Article.ArticleType> types, String status);

    @Query("SELECT a FROM Article a WHERE a.userId = :userId AND a.status <> :status AND a.title LIKE %:keyword% ORDER BY a.createdAt DESC")
    List<Article> searchByUserIdAndTitle(@Param("userId") Long userId, @Param("keyword") String keyword, @Param("status") String status);

    @Query("SELECT a FROM Article a WHERE a.userId = :userId AND a.type IN :types AND a.status <> :status AND a.title LIKE %:keyword% ORDER BY a.createdAt DESC")
    List<Article> searchByUserIdAndTypesAndTitle(@Param("userId") Long userId, @Param("types") List<Article.ArticleType> types, @Param("keyword") String keyword, @Param("status") String status);
}
