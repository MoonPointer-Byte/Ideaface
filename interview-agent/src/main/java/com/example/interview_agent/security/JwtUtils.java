package com.example.interview_agent.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;


@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);


    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private int jwtExpirationMs;


    private SecretKey key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }


    public String generateJwtToken(Authentication authentication) {

        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .subject(userPrincipal.getUsername()) // 将用户名作为JWT的主题(subject)
                .issuedAt(new Date()) // 设置签发时间
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs)) // 设置过期时间
                .signWith(key()) // 使用我们生成的密钥和HMAC-SHA算法进行签名
                .compact(); // 构建并返回最终的JWT字符串
    }


    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(key()) // 使用相同的密钥来验证签名
                .build()
                .parseSignedClaims(token) // 解析签名后的Claims
                .getPayload() // 获取Payload部分
                .getSubject(); // 获取主题，即用户名
    }

    public boolean validateJwtToken(String authToken) {
        try {
            // 尝试解析令牌。如果签名不匹配、已过期或格式错误，这里会抛出异常。
            Jwts.parser().verifyWith(key()).build().parseSignedClaims(authToken);
            return true;
        } catch (Exception e) {
            // 捕获所有可能的解析异常
            logger.error("JWT令牌验证失败: {}", e.getMessage());
        }
        return false;
    }
}