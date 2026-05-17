package com.snowball.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorldVO {
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private String type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
