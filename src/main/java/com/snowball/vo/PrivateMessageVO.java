package com.snowball.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PrivateMessageVO {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String senderName;
    private String body;
    private String imageUrl;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
