package com.snowball.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class JoinRequestVO {
    private Long id;
    private Long worldId;
    private Long applicantId;
    private String applicantName;
    private String reason;
    private String status;
    private LocalDateTime createdAt;
}
