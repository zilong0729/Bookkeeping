package com.bookkeeping.utils;

/**
 * 敏感数据脱敏工具类
 */
public class SensitiveDataUtil {

    /**
     * 手机号脱敏：138****1234
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 邮箱脱敏：a***@example.com
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        int index = email.indexOf("@");
        if (index <= 1) {
            return email;
        }
        return email.substring(0, 1) + "***" + email.substring(index);
    }

    /**
     * 姓名脱敏：张*三 或 王*
     */
    public static String maskName(String name) {
        if (name == null || name.length() <= 1) {
            return name;
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "*" + name.substring(name.length() - 1);
    }

    /**
     * Token脱敏：只保留前6位和后4位
     */
    public static String maskToken(String token) {
        if (token == null || token.length() <= 10) {
            return "***";
        }
        return token.substring(0, 6) + "****" + token.substring(token.length() - 4);
    }

    /**
     * 密码脱敏：一律返回***
     */
    public static String maskPassword(String password) {
        return "***";
    }

    /**
     * 身份证号脱敏：110***********1234
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 10) {
            return idCard;
        }
        return idCard.substring(0, 3) + "***********" + idCard.substring(idCard.length() - 4);
    }
}
