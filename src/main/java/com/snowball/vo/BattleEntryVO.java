package com.snowball.vo;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BattleEntryVO {
    private Long id;
    private Long battleId;
    private Long userId;
    private String username;
    private String title;
    private String body;
    private Double avgScore;
    private Integer voteCount;
    private List<BattleReviewVO> reviews;
    private LocalDateTime createdAt;
}
