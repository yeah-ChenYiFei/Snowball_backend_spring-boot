package com.snowball.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GroupCreateDTO {
    @NotBlank(message = "群名不能为空")
    @Size(max = 100, message = "群名最长100字")
    private String name;
    @Size(max = 500, message = "描述最长500字")
    private String description;
}
