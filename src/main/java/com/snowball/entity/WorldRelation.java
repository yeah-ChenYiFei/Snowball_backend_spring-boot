package com.snowball.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "world_relations")
public class WorldRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "world_id", nullable = false)
    private Long worldId;

    @Column(name = "from_entry_id", nullable = false)
    private Long fromEntryId;

    @Column(name = "to_entry_id", nullable = false)
    private Long toEntryId;

    // JSON array of all entry IDs for multi-entry relations (e.g. "[1,2,3]")
    // For binary relations this can be null; fromEntryId/toEntryId are always set
    @Column(name = "entry_ids", columnDefinition = "TEXT")
    private String entryIds;

    // LEFT_ARROW: from ← to  (to is the ___ of from)
    // RIGHT_ARROW: from → to (from is the ___ of to)
    // BIDIRECTIONAL: from ↔ to
    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private ArrowDirection direction;

    @Column(length = 200)
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum ArrowDirection {
        LEFT_ARROW, RIGHT_ARROW, BIDIRECTIONAL
    }
}
