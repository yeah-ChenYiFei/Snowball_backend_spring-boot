package com.snowball.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SegmentCommentCreateDTO {
    @NotBlank(message = "评论内容不能为空")
    private String body;
}
