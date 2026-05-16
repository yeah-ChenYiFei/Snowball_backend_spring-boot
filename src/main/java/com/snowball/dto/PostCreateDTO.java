package com.snowball.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PostCreateDTO {

    @NotNull(message = "内容类型不能为空")
    private String type; // OC, SETTING, FRAGMENT, BOOK_INFO

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "正文不能为空")
    private String body;

    // tags 和 attachments 先省略，等做标签和文件上传模块时再加
}
