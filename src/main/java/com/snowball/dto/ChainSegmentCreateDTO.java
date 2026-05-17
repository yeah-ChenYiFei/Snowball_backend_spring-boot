package com.snowball.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ChainSegmentCreateDTO {
    private String body;

    @JsonProperty("prevSegmentId")
    private Long prevSegmentId;
}