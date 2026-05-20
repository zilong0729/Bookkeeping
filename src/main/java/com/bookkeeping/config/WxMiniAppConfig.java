package com.bookkeeping.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 微信小程序配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wx.miniapp")
public class WxMiniAppConfig {

    /**
     * 小程序appId
     */
    private String appId;

    /**
     * 小程序appSecret
     */
    private String appSecret;

    /**
     * 微信登录接口地址
     */
    private static final String AUTH_CODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    public String getAuthUrl(String jsCode) {
        return String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                AUTH_CODE2SESSION_URL, appId, appSecret, jsCode);
    }
}
