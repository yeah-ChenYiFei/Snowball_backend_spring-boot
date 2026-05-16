package com.snowball.dto;

import lombok.Data; // ✅ 引入 Lombok

@Data // ✅ 加上这个，删掉下面所有的 getter 和 setter 方法！
public class CommentCreateDTO {
    private String body;
    private Long parentId;
}
