package com.snowball.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorldVO {
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private String type;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Collaboration fields
    private List<CollaboratorVO> collaborators;
    private Boolean isOwner;
    private Boolean isCollaborator;
}
