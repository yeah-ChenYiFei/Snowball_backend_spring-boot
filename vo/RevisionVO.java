// vo/RevisionVO.java
package com.example.snowball.vo;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class RevisionVO {
    private Long id;
    private Long originalPostId;
    private Long authorUserId;
    private String authorName; // ✅ 补全作者名
    private String title;
    private String body;
    private String summary;
    private Integer voteCount;
    private LocalDateTime createdAt;
}