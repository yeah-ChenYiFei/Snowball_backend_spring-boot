package com.example.snowball.dto;

import com.fasterxml.jackson.annotation.JsonProperty; // ✅ 引入 Jackson 的注解
import lombok.Data;

@Data
public class ChainCreateDTO {
    private String title;

    @JsonProperty("first_segment_body") // ✅ 告诉 Spring：前端传下划线没关系，我 Java 内部用驼峰接！
    private String firstSegmentBody;
}
