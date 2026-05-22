package com.snowball.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NovelChapterVO {
    private Long id;
    private Long novelId;
    private String section;
    private Integer volumeNumber;
    private Integer chapterNumber;
    private String title;
    private String body;
    private Integer wordCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
