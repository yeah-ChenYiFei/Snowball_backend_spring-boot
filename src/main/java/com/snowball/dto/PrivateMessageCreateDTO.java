package com.snowball.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PrivateMessageCreateDTO {
    @NotBlank(message = "消息内容不能为空")
    private String body;
    private String imageUrl;
}
