package com.snowball.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ChainCreateDTO {
    private String title;
    private Long groupId;

    @JsonProperty("first_segment_body")
    private String firstSegmentBody;
}
