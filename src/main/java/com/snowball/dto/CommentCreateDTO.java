package com.snowball.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentCreateDTO {
    @NotBlank(message = "评论内容不能为空")
    private String body;
    private Long parentId;
    private String imageUrl;
}
