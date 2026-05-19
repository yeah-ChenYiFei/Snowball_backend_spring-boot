package com.snowball.vo;

import lombok.Data;

@Data
public class FriendshipStatusVO {
    private String status;       // FRIEND, PENDING_TO_THEM, PENDING_FROM_THEM, NONE, SELF
    private Long friendshipId;   // set when PENDING_FROM_THEM so frontend can accept/reject
}
