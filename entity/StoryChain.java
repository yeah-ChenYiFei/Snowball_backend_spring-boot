package com.example.snowball.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "story_chains")
public class StoryChain {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "creator_id", nullable = false)
    private Long creatorId;
    @Column(length = 200, nullable = false)
    private String title;
    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private ChainStatus status = ChainStatus.ONGOING;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
    public enum ChainStatus { ONGOING, FINISHED }
}
