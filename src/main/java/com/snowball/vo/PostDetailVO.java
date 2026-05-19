package com.snowball.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostDetailVO {
    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String body;
    private String status;
    private Long version; // 当前版本号
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String authorName;
    private Integer commentCount;
    private Long likeCount;           // 赞数
    private Long dislikeCount;        // 踩数
    private String currentUserReaction;
    private String chapter;
    private Integer wordCount;
    private Long viewCount;

    private List<String> tags;
}
