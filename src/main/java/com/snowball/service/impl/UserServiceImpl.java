package com.snowball.service.impl;
import com.snowball.common.BusinessException;
import com.snowball.dto.UserLoginDTO;
import com.snowball.dto.UserRegisterDTO;
import com.snowball.entity.User;
import com.snowball.entity.VerificationCode;
import com.snowball.repository.UserRepository;
import com.snowball.security.JwtUtil;
import com.snowball.repository.*;
import com.snowball.service.UserService;
import com.snowball.service.FileStorageService;
import com.snowball.service.PostService;
import com.snowball.service.BookService;
import com.snowball.service.EmailService;
import com.snowball.vo.UserLoginVO;
import com.snowball.vo.UserProfileVO;
import com.snowball.vo.UserVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class UserServiceImpl implements UserService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final PostService postService;
    private final BookService bookService;
    private final FileStorageService fileStorageService;
    private final WorldRepository worldRepository;
    private final WorldCollaboratorRepository worldCollaboratorRepository;
    private final ArticleRepository articleRepository;
    private final InspirationRepository inspirationRepository;
    private final PostRepository postRepository;
    private final EmailService emailService;
    private final VerificationCodeRepository verificationCodeRepository;

    public UserServiceImpl(UserRepository userRepository, JwtUtil jwtUtil, PostService postService, BookService bookService,
                           FileStorageService fileStorageService, WorldRepository worldRepository,
                           WorldCollaboratorRepository worldCollaboratorRepository, ArticleRepository articleRepository,
                           InspirationRepository inspirationRepository, PostRepository postRepository,
                           EmailService emailService, VerificationCodeRepository verificationCodeRepository) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.postService = postService;
        this.bookService = bookService;
        this.fileStorageService = fileStorageService;
        this.worldRepository = worldRepository;
        this.worldCollaboratorRepository = worldCollaboratorRepository;
        this.articleRepository = articleRepository;
        this.inspirationRepository = inspirationRepository;
        this.postRepository = postRepository;
        this.emailService = emailService;
        this.verificationCodeRepository = verificationCodeRepository;
    }

    @Override
    public Long register(UserRegisterDTO dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new BusinessException(400, "用户名已被注册");
        }
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new BusinessException(400, "邮箱已被注册");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setEmailVerified(false);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(user);

        String code = generateVerificationCode();
        VerificationCode vc = new VerificationCode();
        vc.setCode(code);
        vc.setUserId(user.getId());
        vc.setType("REGISTER");
        vc.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        verificationCodeRepository.save(vc);

        try {
            emailService.sendVerificationCode(dto.getEmail(), code);
        } catch (Exception e) {
            log.warn("邮件发送失败（SMTP未配置），验证码: {}", code);
        }

        return user.getId();
    }

    @Override
    public void verifyEmail(Long userId, String code) {
        VerificationCode vc = verificationCodeRepository
                .findByUserIdAndCodeAndTypeAndUsedFalse(userId, code, "REGISTER")
                .orElseThrow(() -> new BusinessException(400, "验证码错误或已使用"));

        if (LocalDateTime.now().isAfter(vc.getExpiresAt())) {
            throw new BusinessException(400, "验证码已过期，请重新获取");
        }

        vc.setUsed(true);
        verificationCodeRepository.save(vc);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Override
    public UserLoginVO login(UserLoginDTO dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new BusinessException(401, "用户名或密码错误"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        if (user.getStatus() == User.UserStatus.DELETED) {
            throw new BusinessException(403, "账号已注销");
        }

        if (Boolean.FALSE.equals(user.getEmailVerified())) {
            throw new BusinessException(403, "邮箱未验证，请先完成邮箱验证");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        UserLoginVO vo = new UserLoginVO();
        vo.setToken(token);
        return vo;
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BusinessException(400, "原密码错误");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void changeUsername(Long userId, String newUsername) {
        if (userRepository.findByUsername(newUsername).isPresent()) {
            throw new BusinessException(400, "用户名已被占用");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        user.setUsername(newUsername);
        userRepository.save(user);
    }

    @Override
    public void changeEmail(Long userId, String newEmail) {
        if (userRepository.findByEmail(newEmail).isPresent()) {
            throw new BusinessException(400, "该邮箱已被其他账号使用");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        String code = generateVerificationCode();
        VerificationCode vc = new VerificationCode();
        vc.setCode(code);
        vc.setUserId(userId);
        vc.setType("CHANGE_EMAIL");
        vc.setEmail(newEmail);
        vc.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        verificationCodeRepository.save(vc);
        try {
            emailService.sendChangeEmailCode(newEmail, code);
        } catch (Exception e) {
            log.warn("邮件发送失败，换绑邮箱验证码: {}", code);
        }
    }

    @Override
    public void verifyNewEmail(Long userId, String code) {
        VerificationCode vc = verificationCodeRepository
                .findByUserIdAndCodeAndTypeAndUsedFalse(userId, code, "CHANGE_EMAIL")
                .orElseThrow(() -> new BusinessException(400, "验证码错误或已使用"));
        if (LocalDateTime.now().isAfter(vc.getExpiresAt())) {
            throw new BusinessException(400, "验证码已过期，请重新获取");
        }
        vc.setUsed(true);
        verificationCodeRepository.save(vc);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        user.setEmail(vc.getEmail());
        userRepository.save(user);
    }

    @Override
    public void deleteAccount(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(400, "密码错误");
        }
        user.setStatus(User.UserStatus.DELETED);
        userRepository.save(user);
    }

    @Override
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(400, "该邮箱未注册"));
        String code = generateVerificationCode();
        VerificationCode vc = new VerificationCode();
        vc.setCode(code);
        vc.setUserId(user.getId());
        vc.setType("RESET_PASSWORD");
        vc.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        verificationCodeRepository.save(vc);
        try {
            emailService.sendPasswordResetCode(email, code);
        } catch (Exception e) {
            log.warn("邮件发送失败，重置密码验证码: {}", code);
        }
    }

    @Override
    public void resetPassword(String email, String code, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(400, "该邮箱未注册"));
        VerificationCode vc = verificationCodeRepository
                .findByUserIdAndCodeAndTypeAndUsedFalse(user.getId(), code, "RESET_PASSWORD")
                .orElseThrow(() -> new BusinessException(400, "验证码错误或已使用"));
        if (LocalDateTime.now().isAfter(vc.getExpiresAt())) {
            throw new BusinessException(400, "验证码已过期，请重新获取");
        }
        vc.setUsed(true);
        verificationCodeRepository.save(vc);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    @Override
    public UserVO getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
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

        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setEmail(user.getEmail());
        userVO.setRole(user.getRole().name());
        userVO.setAvatarUrl(user.getAvatarUrl());
        userVO.setSignature(user.getSignature());
        userVO.setCreatedAt(user.getCreatedAt());
        profileVO.setUser(userVO);

        profileVO.setPosts(postService.getUserPosts(userId));
        profileVO.setBooks(bookService.getMyBooks(userId));

        // Compute real stats
        long worldCount = worldRepository.findByUserIdOrderByCreatedAtDesc(userId).size()
                + worldCollaboratorRepository.findByUserId(userId).size();
        long articleCount = articleRepository.findByUserIdAndStatusNotOrderByCreatedAtDesc(userId, "DELETED").size();
        long inspirationCount = inspirationRepository.findByUserIdOrderByCreatedAtDesc(userId).size();
        long postCount = postRepository.findByUserIdAndStatusNotInOrderByCreatedAtDesc(userId, List.of("HIDDEN", "DELETED")).size();

        Map<String, Long> stats = new HashMap<>();
        stats.put("worlds", worldCount);
        stats.put("articles", articleCount);
        stats.put("inspirations", inspirationCount);
        stats.put("posts", postCount);
        profileVO.setStats(stats);

        return profileVO;
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        String avatarUrl = fileStorageService.saveFile(file, "avatars", "avatar", userId);
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
        return avatarUrl;
    }

    @Override
    public void updateProfile(Long userId, String signature) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        user.setSignature(signature);
        userRepository.save(user);
    }
}
