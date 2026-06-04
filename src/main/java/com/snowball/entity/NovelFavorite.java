package com.snowball.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "novel_favorites",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "novel_id"}),
    indexes = {
        @Index(name = "idx_novel_fav_user", columnList = "user_id"),
        @Index(name = "idx_novel_fav_novel", columnList = "novel_id")
    })
public class NovelFavorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "novel_id", nullable = false)
    private Long novelId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
