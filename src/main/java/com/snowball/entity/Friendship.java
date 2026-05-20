package com.snowball.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "friendships",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "friend_id"}),
    indexes = {
        @Index(name = "idx_friendships_user_id", columnList = "user_id"),
        @Index(name = "idx_friendships_friend_id", columnList = "friend_id")
    })
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "friend_id", nullable = false)
    private Long friendId;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private FriendshipStatus status;

    public enum FriendshipStatus { PENDING, ACCEPTED, REJECTED }

    @Column(length = 20, nullable = false)
    private String source; // POST, GROUP, PROFILE

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
