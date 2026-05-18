package com.snowball.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PostUpdateDTO {
    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "正文不能为空")
    private String body;

    private String chapter;

    private String changeSummary = "常规修改"; // 变更摘要，默认值
}
