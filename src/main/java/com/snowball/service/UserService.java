package com.snowball.service;
import com.snowball.dto.UserLoginDTO;
import com.snowball.dto.UserRegisterDTO;
import com.snowball.vo.UserLoginVO;
import com.snowball.vo.UserProfileVO;
import com.snowball.vo.UserVO;
import java.util.Map;
public interface UserService {
    void register(UserRegisterDTO dto);
    UserLoginVO login(UserLoginDTO dto);
    UserVO getCurrentUser(Long userId);
    UserProfileVO getUserProfile(Long userId);
}
