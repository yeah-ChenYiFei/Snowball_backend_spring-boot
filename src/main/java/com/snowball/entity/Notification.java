package com.snowball.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 30)
    private String type;
    // COMMENT, REPLY, LIKE_POST, LIKE_COMMENT, DISLIKE_POST, DISLIKE_COMMENT, GROUP_JOIN

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "source_type", length = 30)
    private String sourceType; // POST, COMMENT, GROUP, BATTLE

    @Column(name = "actor_id")
    private Long actorId;

    @Column(length = 500, nullable = false)
    private String body;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
