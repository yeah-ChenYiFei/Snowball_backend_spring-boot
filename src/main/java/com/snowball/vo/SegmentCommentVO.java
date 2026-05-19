package com.snowball.vo;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class SegmentCommentVO {
    private Long id;
    private Long userId;
    private String username;
    private String body;
    private LocalDateTime createdAt;
}
