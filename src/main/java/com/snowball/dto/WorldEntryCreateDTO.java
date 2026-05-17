package com.snowball.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorldEntryCreateDTO {
    @NotBlank(message = "设定名称不能为空")
    private String name;

    private String type;

    @NotBlank(message = "内容不能为空")
    private String content;
}
