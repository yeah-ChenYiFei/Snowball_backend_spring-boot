package com.example.snowball.controller;

import com.example.snowball.common.BusinessException;
import com.example.snowball.common.Result;
import com.example.snowball.entity.Book;
import com.example.snowball.entity.Post;
import com.example.snowball.entity.User;
import com.example.snowball.repository.BookRepository;
import com.example.snowball.repository.PostRepository;
import com.example.snowball.repository.UserRepository;
import com.example.snowball.vo.UserProfileVO;
import com.example.snowball.vo.UserVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final BookRepository bookRepository;

    public UserController(UserRepository userRepository, PostRepository postRepository, BookRepository bookRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.bookRepository = bookRepository;
    }

    // 对应文档 5.2.1：获取个人主页聚合数据
    @GetMapping("/{id}/profile")
    public Result<UserProfileVO> getProfile(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        List<Post> posts = postRepository.findByUserIdOrderByCreatedAtDesc(id, org.springframework.data.domain.PageRequest.of(0, 10)).getContent();
        List<Book> books = bookRepository.findByUserIdOrderByPurchaseDateDesc(id);

        UserProfileVO vo = new UserProfileVO();

        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setAvatarUrl(user.getAvatarUrl());
        vo.setUser(userVO);
        vo.setPosts(posts);
        vo.setBooks(books);

        return Result.success(vo);
    }
}
