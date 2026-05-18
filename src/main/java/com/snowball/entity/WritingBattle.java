package com.snowball.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "writing_battles")
public class WritingBattle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "creator_id", nullable = false)
    private Long creatorId;

    @Column(length = 200, nullable = false)
    private String topic;

    @Column(length = 1000)
    private String description;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "participant_ids", length = 500)
    private String participantIds; // comma-separated user IDs

    @Column(length = 20, nullable = false)
    private String status = "OPEN";
    // OPEN | VOTING | CLOSED

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
