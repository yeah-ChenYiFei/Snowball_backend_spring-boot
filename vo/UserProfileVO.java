package com.example.snowball.vo;
import com.example.snowball.entity.Book;
import com.example.snowball.entity.Post;
import lombok.Data;
import java.util.List;

@Data
public class UserProfileVO {
    private UserVO user;
    private List<Post> posts;
    private List<Book> books;
}
