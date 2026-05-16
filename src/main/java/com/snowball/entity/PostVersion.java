package com.snowball.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "post_versions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "version_number"}))
public class PostVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "body_snapshot", columnDefinition = "LONGTEXT", nullable = false)
    private String bodySnapshot;

    @Column(name = "change_summary", length = 500)
    private String changeSummary;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
