package com.snowball.dto;
import lombok.Data;

@Data
public class GroupMessageCreateDTO {
    private String body;
    private String type;   // optional, defaults to CHAT
    private Long refId;    // optional
    private String refType; // optional: CHAIN | BATTLE
}
