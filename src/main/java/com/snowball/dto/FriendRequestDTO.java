package com.snowball.dto;

import lombok.Data;

@Data
public class FriendRequestDTO {
    private Long friendId;
    private String source;   // POST, GROUP, PROFILE
    private Long sourceId;
}
