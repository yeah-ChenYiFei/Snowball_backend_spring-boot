package com.snowball.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "world_changes")
public class WorldChange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "world_id", nullable = false)
    private Long worldId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "entry_id")
    private Long entryId;

    @Column(name = "entry_name", length = 200, nullable = false)
    private String entryName;

    @Column(name = "entry_type", length = 50)
    private String entryType;

    @Column(name = "entry_content", columnDefinition = "LONGTEXT", nullable = false)
    private String entryContent;

    @Column(name = "change_type", length = 20, nullable = false)
    private String changeType; // CREATE, UPDATE, DELETE

    @Column(length = 20, nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
