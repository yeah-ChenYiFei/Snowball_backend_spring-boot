package com.snowball.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InspirationVO {
    private Long id;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
