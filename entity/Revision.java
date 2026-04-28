package com.example.snowball.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "revisions")
public class Revision {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "original_post_id", nullable = false)
    private Long originalPostId;
    @Column(name = "author_user_id", nullable = false)
    private Long authorUserId;
    @Column(length = 200, nullable = false)
    private String title;
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String body;
    @Column(length = 500)
    private String summary;
    @Column(name = "vote_count")
    private Integer voteCount = 0;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
