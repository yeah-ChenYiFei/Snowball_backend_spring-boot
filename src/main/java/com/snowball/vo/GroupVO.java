package com.snowball.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GroupVO {
    private Long id;
    private String name;
    private Long creatorId;
    private Boolean isPrivate;
    private LocalDateTime createdAt;
}
