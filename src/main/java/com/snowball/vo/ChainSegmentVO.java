package com.snowball.vo;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class ChainSegmentVO {
    private Long id;
    private Long userId;
    private String username; // ✅ 记得补作者名
    private String body;
    private Long prevSegmentId;
    private Integer depth;
    private LocalDateTime createdAt;
}