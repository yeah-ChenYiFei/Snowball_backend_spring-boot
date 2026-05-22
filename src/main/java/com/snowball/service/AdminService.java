package com.snowball.service;

import com.snowball.vo.UserVO;
import java.util.List;
import java.util.Map;

public interface AdminService {
    Map<String, Long> getStats();
    List<UserVO> listUsers(String search, String role, String status, int page, int size);
    void updateUserRole(Long userId, String role, Long operatorId);
    void updateUserStatus(Long userId, String status);
    List<Map<String, Object>> listArticles(String search, String status, int page, int size);
    List<Map<String, Object>> listWorlds(String search, String isPublic, int page, int size);
    void unpublishArticle(Long id);
    void publishArticle(Long id);
    void deleteWorld(Long id);
}
