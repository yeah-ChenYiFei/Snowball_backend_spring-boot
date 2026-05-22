package com.snowball.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NovelUpdateDTO {
    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;

    private Boolean hasVolumes;
}
