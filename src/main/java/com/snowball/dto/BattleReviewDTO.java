package com.snowball.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BattleReviewDTO {
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1分")
    @Max(value = 10, message = "评分最高10分")
    private Integer score;
    @Size(max = 500, message = "评语最长500字")
    private String comment;
}
