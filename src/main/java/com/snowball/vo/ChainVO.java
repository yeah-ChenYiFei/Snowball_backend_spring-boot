package com.snowball.vo;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class ChainVO {
    private Long id;
    private Long creatorId;
    private String title;
    private String status;
    private LocalDateTime createdAt;
}