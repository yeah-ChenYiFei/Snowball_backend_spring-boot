package com.snowball.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NovelVO {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private Boolean hasVolumes;
    private Long worldId;
    private String worldName;
    private Boolean isPublished;
    private LocalDateTime publishedAt;
    private Integer chapterCount;
    private Integer totalWordCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String authorName;
}
