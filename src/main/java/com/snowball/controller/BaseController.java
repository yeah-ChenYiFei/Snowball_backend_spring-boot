package com.snowball.controller;

import com.snowball.common.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

// 让所有 Controller 继承这个基类
public class BaseController {

    /**
     * 获取当前登录用户 ID（必须登录的接口用这个）
     */
    protected Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "用户未登录");
        }
        return (Long) authentication.getPrincipal();
    }

    /**
     * 尝试获取当前用户 ID（游客也能访问的接口用这个，比如广场列表）
     */
    protected Long getOptionalUserId() {
        try {
            return getCurrentUserId();
        } catch (Exception e) {
            return null; // 游客返回 null
        }
    }
}
