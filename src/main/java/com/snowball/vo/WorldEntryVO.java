package com.snowball.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorldEntryVO {
    private Long id;
    private Long worldId;
    private Long userId;
    private String name;
    private String type;
    private String content;
    private String contentPreview;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
