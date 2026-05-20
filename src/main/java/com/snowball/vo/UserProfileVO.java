package com.snowball.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class UserProfileVO {
    private UserVO user;
    private List<PostDetailVO> posts;
    private List<BookVO> books;
    private Map<String, Long> stats;
}
