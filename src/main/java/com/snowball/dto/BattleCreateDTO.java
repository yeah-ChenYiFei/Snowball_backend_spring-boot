package com.snowball.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BattleCreateDTO {
    @NotBlank(message = "擂台主题不能为空")
    @Size(max = 200, message = "主题最长200字")
    private String topic;
    private String description;
    private LocalDateTime deadline;
    private List<Long> participantIds;
}
