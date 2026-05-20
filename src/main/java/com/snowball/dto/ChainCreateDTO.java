package com.snowball.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChainCreateDTO {
    @NotBlank(message = "接龙标题不能为空")
    @Size(max = 200, message = "标题最长200字")
    private String title;
    private String description;
    private LocalDateTime deadline;
    private Long groupId;  // null = public chain

    @JsonProperty("first_segment_body")
    @NotBlank(message = "首个段落不能为空")
    private String firstSegmentBody;
}
