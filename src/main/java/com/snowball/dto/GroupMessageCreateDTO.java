package com.snowball.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GroupMessageCreateDTO {
    @NotBlank(message = "消息内容不能为空")
    private String body;
    private String imageUrl;
    private String type;   // optional, defaults to CHAT
    private Long refId;    // optional
    private String refType; // optional: CHAIN | BATTLE
}
