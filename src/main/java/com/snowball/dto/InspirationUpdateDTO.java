package com.snowball.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InspirationUpdateDTO {
    @NotBlank(message = "内容不能为空")
    private String content;
}
