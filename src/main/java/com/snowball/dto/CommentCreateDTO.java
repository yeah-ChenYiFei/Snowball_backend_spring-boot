package com.snowball.dto;

import lombok.Data;

@Data
public class CommentCreateDTO {
    private String body;
    private Long parentId;
    private String imageUrl;
}
