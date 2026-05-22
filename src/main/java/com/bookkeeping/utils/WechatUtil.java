package com.bookkeeping.utils;

import com.bookkeeping.config.WechatConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
@RequiredArgsConstructor
public class WechatUtil {

    private final WechatConfig wechatConfig;
    private final ObjectMapper objectMapper;

    private String accessToken;
    private long tokenExpireTime = 0;
    private final ReentrantLock lock = new ReentrantLock();

    public String getAccessToken() {
        long now = System.currentTimeMillis();
        if (accessToken != null && now < tokenExpireTime) {
            return accessToken;
        }

        lock.lock();
        try {
            if (accessToken != null && now < tokenExpireTime) {
                return accessToken;
            }

            String url = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                    wechatConfig.getAppId(), wechatConfig.getAppSecret());

            String response = httpGet(url);
            JsonNode jsonNode = objectMapper.readTree(response);

            if (jsonNode.has("access_token")) {
                accessToken = jsonNode.get("access_token").asText();
                int expiresIn = jsonNode.has("expires_in") ? jsonNode.get("expires_in").asInt(7200) : 7200;
                tokenExpireTime = now + (expiresIn - 60) * 1000L;
                log.info("获取access_token成功");
                return accessToken;
            } else {
                String errMsg = jsonNode.has("errmsg") ? jsonNode.get("errmsg").asText() : "未知错误";
                log.error("获取access_token失败: {}", errMsg);
                throw new RuntimeException("获取access_token失败: " + errMsg);
            }
        } catch (Exception e) {
            log.error("获取access_token异常", e);
            throw new RuntimeException("获取access_token异常", e);
        } finally {
            lock.unlock();
        }
    }

    public boolean sendSubscribeMessage(String openId, String templateId, Map<String, String> data) {
        try {
            String accessToken = getAccessToken();
            String url = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + accessToken;

            Map<String, Object> message = new HashMap<>();
            message.put("touser", openId);
            message.put("template_id", templateId);
            message.put("page", "/pages/index/index");

            Map<String, Map<String, String>> dataMap = new HashMap<>();
            for (Map.Entry<String, String> entry : data.entrySet()) {
                Map<String, String> value = new HashMap<>();
                value.put("value", entry.getValue());
                dataMap.put(entry.getKey(), value);
            }
            message.put("data", dataMap);

            String json = objectMapper.writeValueAsString(message);
            String response = httpPost(url, json);

            JsonNode jsonNode = objectMapper.readTree(response);
            int errcode = jsonNode.has("errcode") ? jsonNode.get("errcode").asInt(-1) : -1;

            if (errcode == 0) {
                log.info("发送订阅消息成功: openId={}", openId);
                return true;
            } else {
                String errMsg = jsonNode.has("errmsg") ? jsonNode.get("errmsg").asText() : "未知错误";
                log.error("发送订阅消息失败: openId={}, errcode={}, errmsg={}", openId, errcode, errMsg);
                return false;
            }
        } catch (Exception e) {
            log.error("发送订阅消息异常: openId={}", openId, e);
            return false;
        }
    }

    private String httpGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        conn.disconnect();
        return sb.toString();
    }

    private String httpPost(String urlStr, String body) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        conn.disconnect();
        return sb.toString();
    }
}
