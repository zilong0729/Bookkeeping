package com.bookkeeping.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Slf4j
@Component
public class SensitiveDataUtil {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^\\d{15}$|^\\d{17}[\\dXx]$");

    public static String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return null;
        }
        if (PHONE_PATTERN.matcher(phone).matches()) {
            return phone.substring(0, 3) + "****" + phone.substring(7);
        }
        return phone;
    }

    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }
        if (EMAIL_PATTERN.matcher(email).matches()) {
            int atIndex = email.indexOf('@');
            if (atIndex > 2) {
                return email.substring(0, 2) + "***" + email.substring(atIndex);
            }
        }
        return email;
    }

    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.isEmpty()) {
            return null;
        }
        if (ID_CARD_PATTERN.matcher(idCard).matches()) {
            if (idCard.length() == 15) {
                return "****" + idCard.substring(12);
            } else {
                return "************" + idCard.substring(14);
            }
        }
        return idCard;
    }

    public static String maskToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        if (token.length() > 20) {
            return token.substring(0, 10) + "****" + token.substring(token.length() - 10);
        }
        return "****";
    }

    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        if (name.length() == 1) {
            return name;
        } else if (name.length() == 2) {
            return name.substring(0, 1) + "*";
        } else {
            return "*".repeat(name.length());
        }
    }

    public static String maskBankCard(String bankCard) {
        if (bankCard == null || bankCard.isEmpty()) {
            return null;
        }
        if (bankCard.length() >= 8) {
            return "**** **** **** " + bankCard.substring(bankCard.length() - 4);
        }
        return "****";
    }

    public static String maskAddress(String address) {
        if (address == null || address.isEmpty()) {
            return null;
        }
        if (address.length() <= 6) {
            return "***";
        }
        return address.substring(0, 3) + "***" + address.substring(address.length() - 3);
    }

    public static String maskPassword(String password) {
        if (password == null || password.isEmpty()) {
            return null;
        }
        return "******";
    }

    public static String maskPwd(String pwd) {
        return maskPassword(pwd);
    }
}
