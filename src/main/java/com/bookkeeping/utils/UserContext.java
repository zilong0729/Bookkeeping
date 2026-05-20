package com.bookkeeping.utils;

/**
 * 用户上下文工具类
 * 用于存储当前登录用户信息
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> OPENID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<Integer> ROLE_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    /**
     * 设置当前用户openid
     */
    public static void setOpenid(String openid) {
        OPENID_HOLDER.set(openid);
    }

    /**
     * 获取当前用户openid
     */
    public static String getOpenid() {
        return OPENID_HOLDER.get();
    }

    /**
     * 设置当前用户角色
     */
    public static void setRole(Integer role) {
        ROLE_HOLDER.set(role);
    }

    /**
     * 获取当前用户角色
     */
    public static Integer getRole() {
        return ROLE_HOLDER.get();
    }

    /**
     * 判断当前用户是否为管理员
     */
    public static boolean isAdmin() {
        Integer role = ROLE_HOLDER.get();
        return role != null && role == 1;
    }

    /**
     * 清除当前用户信息
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
        OPENID_HOLDER.remove();
        ROLE_HOLDER.remove();
    }
}
