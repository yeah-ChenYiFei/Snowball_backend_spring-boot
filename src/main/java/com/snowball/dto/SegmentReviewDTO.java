package com.snowball.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SegmentReviewDTO {
    @NotBlank(message = "审核状态不能为空")
    private String status; // APPROVED or REJECTED
}
