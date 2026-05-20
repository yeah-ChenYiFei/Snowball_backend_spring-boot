package com.snowball.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TagCreateDTO {
    @NotBlank(message = "标签名不能为空")
    @Size(max = 50, message = "标签名最长50字")
    private String name;
    @Size(max = 200, message = "描述最长200字")
    private String description;
}
