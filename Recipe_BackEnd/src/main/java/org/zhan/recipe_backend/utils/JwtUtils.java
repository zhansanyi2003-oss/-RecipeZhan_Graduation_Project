package org.zhan.recipe_backend.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    private static final String SECRET_STRING = "WhatShouldIEatMySuperSecretKeyDoNotShare123456";
    private static final Key SECRET_KEY =Keys.hmacShaKeyFor(SECRET_STRING.getBytes());
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;

    // 1. 生成 Token (传入用户名和角色)
    public String generateToken(Long id, String username, String role, String session) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .claim("userId", id)
                .claim("sessionId", session)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}