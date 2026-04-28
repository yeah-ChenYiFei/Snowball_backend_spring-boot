package com.example.snowball.service;
import com.example.snowball.dto.UserLoginDTO;
import com.example.snowball.dto.UserRegisterDTO;
import com.example.snowball.vo.UserVO;
import java.util.Map;
public interface UserService {
    void register(UserRegisterDTO dto);
    Map<String, Object> login(UserLoginDTO dto);
    UserVO getCurrentUser(Long userId);
}
