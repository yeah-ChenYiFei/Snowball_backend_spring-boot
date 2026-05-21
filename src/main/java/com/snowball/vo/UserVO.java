package com.snowball.vo;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class UserVO {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String avatarUrl;
    private String signature;
    private LocalDateTime createdAt;
}
