package com.snowball.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FriendRequestDTO {
    @NotNull(message = "好友ID不能为空")
    private Long friendId;
    private String source;   // POST, GROUP, PROFILE
    private Long sourceId;
}
