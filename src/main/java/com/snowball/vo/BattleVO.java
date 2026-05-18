package com.snowball.vo;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BattleVO {
    private Long id;
    private Long groupId;
    private Long creatorId;
    private String creatorName;
    private String topic;
    private String description;
    private LocalDateTime deadline;
    private String status;
    private List<BattleEntryVO> entries;
    private LocalDateTime createdAt;
}
