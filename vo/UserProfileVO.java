package com.example.snowball.vo;

import lombok.Data;
import java.util.List;

@Data
public class UserProfileVO {
    private UserVO user;
    private List<PostDetailVO> posts; // ✅ 炸弹拆除：换成 PostDetailVO
    private List<BookVO> books;       // ✅ 炸弹拆除：换成 BookVO
}
