package com.snowball.vo;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BattleReviewVO {
    private Long id;
    private Long entryId;
    private Long reviewerId;
    private String reviewerName;
    private Integer score;
    private String comment;
    private LocalDateTime createdAt;
}
