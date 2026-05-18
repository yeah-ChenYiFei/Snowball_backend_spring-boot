package com.snowball.dto;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BattleCreateDTO {
    private String topic;
    private String description;
    private LocalDateTime deadline;
    private List<Long> participantIds; // user IDs of assigned writers
}
