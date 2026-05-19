package com.snowball.dto;

import lombok.Data;

@Data
public class WorldChangeActionDTO {
    private String action;    // APPROVE or REJECT
    private String rejectReason;
}
