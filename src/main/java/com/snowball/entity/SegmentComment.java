package com.snowball.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "segment_comments")
public class SegmentComment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "segment_id", nullable = false)
    private Long segmentId;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
