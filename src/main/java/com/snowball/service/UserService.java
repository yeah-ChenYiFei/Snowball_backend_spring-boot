package com.snowball.service;
import com.snowball.dto.UserLoginDTO;
import com.snowball.dto.UserRegisterDTO;
import com.snowball.vo.UserLoginVO;
import com.snowball.vo.UserProfileVO;
import com.snowball.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    Long register(UserRegisterDTO dto);
    void verifyEmail(Long userId, String code);
    UserLoginVO login(UserLoginDTO dto);
    UserVO getCurrentUser(Long userId);
    UserProfileVO getUserProfile(Long userId);
    String uploadAvatar(Long userId, MultipartFile file);
    void updateProfile(Long userId, String signature);
    void changePassword(Long userId, String oldPassword, String newPassword);
    void changeUsername(Long userId, String newUsername);
    void forgotPassword(String email);
    void resetPassword(String email, String code, String newPassword);
    void changeEmail(Long userId, String newEmail);
    void verifyNewEmail(Long userId, String code);
    void deleteAccount(Long userId, String password);
}
