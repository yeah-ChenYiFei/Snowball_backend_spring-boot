package com.snowball.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "comments")
public class Comment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "post_id")
    private Long postId;

    @Column(name = "source_type", length = 20)
    private String sourceType = "POST";

    @Column(name = "source_id")
    private Long sourceId;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "parent_id")
    private Long parentId; // 父评论ID，为空则是顶级评论
    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
    @Column(name = "like_count")
    private Integer likeCount = 0;
    @Column(name = "dislike_count")
    private Integer dislikeCount = 0;
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
