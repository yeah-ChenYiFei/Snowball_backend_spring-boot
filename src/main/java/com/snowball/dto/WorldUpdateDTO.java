package com.snowball.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WorldUpdateDTO {
    @Size(max = 100, message = "名称最长100字")
    private String name;
    @Size(max = 500, message = "描述最长500字")
    private String description;
    @Size(max = 50, message = "类型最长50字")
    private String type;
    private Boolean isPublic;
}
