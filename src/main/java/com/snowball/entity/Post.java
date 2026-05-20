package com.snowball.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private PostType type;

    @Column(length = 200, nullable = false)
    private String title;

    @Column(name = "current_body", columnDefinition = "LONGTEXT")
    private String currentBody;

    @Column(length = 10)
    private String status = "public";

    // 文档 3.1.4 提到的乐观锁！加上这个注解，Hibernate 会自动管理 version 字段
    @Version
    private Long version;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Column(length = 200)
    private String chapter;

    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(columnDefinition = "TEXT")
    private String images;

    public enum PostType { OC, SETTING, FRAGMENT, BOOK_INFO, ESSAY, DIARY, NOVEL, THOUGHT }
}
