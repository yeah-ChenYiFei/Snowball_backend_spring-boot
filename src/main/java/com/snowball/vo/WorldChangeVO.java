package com.snowball.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorldChangeVO {
    private Long id;
    private Long worldId;
    private Long userId;
    private String username;
    private Long entryId;
    private String entryName;
    private String entryType;
    private String entryContent;
    private String changeType;   // CREATE, UPDATE, DELETE
    private String status;       // PENDING, APPROVED, REJECTED
    private String rejectReason;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}
