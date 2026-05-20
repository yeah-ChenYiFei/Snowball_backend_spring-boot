package com.snowball.vo;

import lombok.Data;

@Data
public class GroupMemberVO {
    private Long userId;
    private String username;
    private String avatarUrl;
    private String role;
}
