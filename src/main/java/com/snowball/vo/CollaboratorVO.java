package com.snowball.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CollaboratorVO {
    private Long userId;
    private String username;
    private String avatarUrl;
    private String role;
    private LocalDateTime since;
}
