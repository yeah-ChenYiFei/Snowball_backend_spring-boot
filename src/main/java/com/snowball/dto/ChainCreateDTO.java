package com.snowball.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChainCreateDTO {
    private String title;
    private String description;
    private LocalDateTime deadline;
    private Long groupId;  // null = public chain

    @JsonProperty("first_segment_body")
    private String firstSegmentBody;
}
