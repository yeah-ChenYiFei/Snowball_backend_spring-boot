package com.snowball.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChainDetailVO {
    private Long id;
    private Long creatorId;
    private String creatorName;
    private String title;
    private String status;
    private LocalDateTime createdAt;
    private List<ChainSegmentVO> segments;
}
