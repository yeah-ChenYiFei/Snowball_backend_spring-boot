package com.snowball.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "join_requests")
public class JoinRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "world_id", nullable = false)
    private Long worldId;

    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private JoinRequestStatus status = JoinRequestStatus.PENDING;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    public enum JoinRequestStatus { PENDING, APPROVED, REJECTED }
}
