package com.snowball.vo;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GroupMessageVO {
    private Long id;
    private Long groupId;
    private Long senderId;
    private String senderName;
    private String body;
    private String imageUrl;
    private String senderAvatarUrl;
    private String type;
    private Long refId;
    private String refType;
    private LocalDateTime createdAt;
}
