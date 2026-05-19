package com.snowball.vo;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class ChainVO {
    private Long id;
    private Long creatorId;
    private String creatorName;
    private String title;
    private String description;
    private String status;
    private Long groupId;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private String firstSegmentBody;
    private int segmentCount;
}