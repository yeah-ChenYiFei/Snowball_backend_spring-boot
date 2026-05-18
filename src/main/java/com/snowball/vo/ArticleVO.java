package com.snowball.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ArticleVO {
    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String body;
    private String status;
    private String chapter;
    private Integer wordCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String authorName;
}
