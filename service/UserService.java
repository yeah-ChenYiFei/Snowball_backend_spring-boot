package com.example.snowball.service;
import com.example.snowball.dto.UserLoginDTO;
import com.example.snowball.dto.UserRegisterDTO;
import com.example.snowball.vo.UserLoginVO;
import com.example.snowball.vo.UserProfileVO;
import com.example.snowball.vo.UserVO;
import java.util.Map;
public interface UserService {
    void register(UserRegisterDTO dto);
    UserLoginVO login(UserLoginDTO dto);
    UserVO getCurrentUser(Long userId);
    UserProfileVO getUserProfile(Long userId);
}
