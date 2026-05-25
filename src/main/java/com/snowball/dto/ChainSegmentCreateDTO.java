package com.snowball.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChainSegmentCreateDTO {
    @NotBlank(message = "段落内容不能为空")
    private String body;

    @JsonProperty("prevSegmentId")
    private Long prevSegmentId;

    @JsonProperty("isAi")
    private Boolean isAi;
}
