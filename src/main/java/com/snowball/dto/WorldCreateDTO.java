package com.snowball.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorldCreateDTO {
    @NotBlank(message = "世界名称不能为空")
    private String name;

    private String description;

    private String type;

    private Boolean isPublic = true;
}
