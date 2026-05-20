package com.snowball.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "`groups`")
public class Group {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 100, nullable = false)
    private String name;
    @Column(length = 500)
    private String description;
    @Column(name = "creator_id", nullable = false)
    private Long creatorId;
    @Column(name = "is_private")
    private Boolean isPrivate = true;
    @Column(name = "is_searchable")
    private Boolean isSearchable = false;
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;
    @Column(name = "group_number", unique = true)
    private Long groupNumber;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
