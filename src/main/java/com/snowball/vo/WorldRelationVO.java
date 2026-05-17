package com.snowball.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorldRelationVO {
    private Long id;
    private Long worldId;
    private Long fromEntryId;
    private String fromEntryName;
    private Long toEntryId;
    private String toEntryName;
    private String direction;
    private String description;
    private LocalDateTime createdAt;
}
