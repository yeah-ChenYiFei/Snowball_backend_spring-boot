package com.snowball.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BattleEntryDTO {
    @NotBlank(message = "作品标题不能为空")
    @Size(max = 200, message = "标题最长200字")
    private String title;
    @NotBlank(message = "作品内容不能为空")
    private String body;
}
