package com.snowball.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "chain_segments", indexes = {
    @Index(name = "idx_cs_chain_id", columnList = "chain_id"),
    @Index(name = "idx_cs_user_id", columnList = "user_id"),
    @Index(name = "idx_cs_prev_seg_id", columnList = "prev_segment_id")
})
public class ChainSegment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "chain_id", nullable = false)
    private Long chainId;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String body;
    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private SegmentStatus status = SegmentStatus.PENDING;
    @Column(name = "prev_segment_id")
    private Long prevSegmentId;
    @Column(nullable = false)
    private Integer depth = 1;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public enum SegmentStatus { PENDING, APPROVED, REJECTED }
}
