package com.snowball.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "story_chains", indexes = {
    @Index(name = "idx_sc_creator_id", columnList = "creator_id"),
    @Index(name = "idx_sc_group_id", columnList = "group_id")
})
public class StoryChain {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "creator_id", nullable = false)
    private Long creatorId;
    @Column(name = "group_id")
    private Long groupId;  // null = global chain, non-null = group-scoped

    @Column(length = 200, nullable = false)
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private ChainStatus status = ChainStatus.ONGOING;
    private LocalDateTime deadline;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
    public enum ChainStatus { ONGOING, FINISHED }
}
