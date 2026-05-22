package com.snowball.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "post_favorites",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_id"}),
    indexes = {
        @Index(name = "idx_post_fav_user", columnList = "user_id"),
        @Index(name = "idx_post_fav_post", columnList = "post_id")
    })
public class PostFavorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
