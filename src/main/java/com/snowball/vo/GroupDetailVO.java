package com.snowball.vo;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GroupDetailVO {
    private Long id;
    private String name;
    private String description;
    private Long creatorId;
    private String creatorName;
    private Boolean isPrivate;
    private Boolean isSearchable;
    private String avatarUrl;
    private Long groupNumber;
    private Long memberCount;
    private List<GroupMemberVO> members;
    private LocalDateTime createdAt;
}
