package com.example.snowball.service.impl;
import com.example.snowball.common.BusinessException;
import com.example.snowball.dto.UserLoginDTO;
import com.example.snowball.dto.UserRegisterDTO;
import com.example.snowball.entity.User;
import com.example.snowball.repository.UserRepository;
import com.example.snowball.security.JwtUtil;
import com.example.snowball.service.UserService;
import com.example.snowball.service.PostService;
import com.example.snowball.service.BookService;
import com.example.snowball.vo.UserLoginVO;
import com.example.snowball.vo.UserProfileVO;
import com.example.snowball.vo.UserVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); // 对应概要 BCrypt 加密
    private final PostService postService;
    private final BookService bookService;

    public UserServiceImpl(UserRepository userRepository, JwtUtil jwtUtil, PostService postService, BookService bookService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.postService = postService;
        this.bookService = bookService;
    }

    @Override
    public void register(UserRegisterDTO dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new BusinessException(400, "用户名已被注册");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword())); // 加密存储
        userRepository.save(user);
    }

    @Override
    public UserLoginVO login(UserLoginDTO dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new BusinessException(401, "用户名或密码错误"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        UserLoginVO vo = new UserLoginVO();
        vo.setToken(token);
        return vo;
    }

    @Override
    public UserVO getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRole(user.getRole().name());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }
    @Override
    public UserProfileVO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));

        UserProfileVO profileVO = new UserProfileVO();

        // 1. 组装基础用户信息
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setAvatarUrl(user.getAvatarUrl());
        profileVO.setUser(userVO);

        // 2. ✅ 炸弹拆除：不再查 Repository，而是调 PostService 拿 VO 列表！
        profileVO.setPosts(postService.getAllPosts(userId));

        // 3. ✅ 炸弹拆除：调 BookService 拿 VO 列表！
        profileVO.setBooks(bookService.getMyBooks(userId));

        return profileVO;
    }
}
