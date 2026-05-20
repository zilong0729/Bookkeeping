package com.bookkeeping.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT工具类
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_OPENID = "openid";
    private static final String CLAIM_ROLE = "role";

    /**
     * 生成token
     */
    public String generateToken(Long userId, String openid, Integer role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return JWT.create()
                .withClaim(CLAIM_USER_ID, userId)
                .withClaim(CLAIM_OPENID, openid)
                .withClaim(CLAIM_ROLE, role)
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .sign(Algorithm.HMAC256(secret));
    }

    /**
     * 验证token
     */
    public boolean validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(CLAIM_USER_ID).asLong();
        } catch (Exception e) {
            log.error("从token解析用户ID失败", e);
            return null;
        }
    }

    /**
     * 从token中获取openid
     */
    public String getOpenidFromToken(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(CLAIM_OPENID).asString();
        } catch (Exception e) {
            log.error("从token解析openid失败", e);
            return null;
        }
    }

    /**
     * 从token中获取用户角色
     */
    public Integer getRoleFromToken(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(CLAIM_ROLE).asInt();
        } catch (Exception e) {
            log.error("从token解析角色失败", e);
            return null;
        }
    }

    /**
     * 获取token过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getExpiresAt();
        } catch (Exception e) {
            log.error("从token解析过期时间失败", e);
            return null;
        }
    }
}
