package com.snowball.dto;
import lombok.Data;
@Data
public class ChainSegmentCreateDTO {
    private String body;
    private String prev_segment_id; // 接在谁后面
}