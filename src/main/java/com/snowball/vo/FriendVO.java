package com.snowball.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FriendVO {
    private Long userId;
    private String username;
    private String avatarUrl;
    private LocalDateTime since;
}
