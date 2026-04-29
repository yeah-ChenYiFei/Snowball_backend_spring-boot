package com.example.snowball.vo;

import lombok.Data;

@Data
public class GroupMemberVO {
    private Long userId;
    private String username; // 聚合数据
    private String role;
}
