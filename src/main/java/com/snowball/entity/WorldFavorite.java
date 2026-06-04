package com.snowball.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "world_favorites",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "world_id"}),
    indexes = {
        @Index(name = "idx_world_fav_user", columnList = "user_id"),
        @Index(name = "idx_world_fav_world", columnList = "world_id")
    })
public class WorldFavorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "world_id", nullable = false)
    private Long worldId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
