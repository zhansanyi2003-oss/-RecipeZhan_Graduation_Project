package org.zhan.recipe_backend.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtils {

    /**
     * 获取当前登录用户的 ID，如果是游客则返回 null
     */
    public static Long getCurrentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return null; // 是游客
        }

        // 是登录用户，返回 ID
        return (Long) authentication.getPrincipal();
    }

    public static boolean isGuest() {
        return getCurrentUserIdOrNull() == null;
    }
}