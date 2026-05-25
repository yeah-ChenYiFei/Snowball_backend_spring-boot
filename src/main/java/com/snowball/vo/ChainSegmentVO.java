package com.snowball.vo;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class ChainSegmentVO {
    private Long id;
    private Long userId;
    private String username;
    private String body;
    private String status;
    private Long prevSegmentId;
    private Integer depth;
    private int commentCount;
    private Boolean isAiGenerated;
    private LocalDateTime createdAt;
}