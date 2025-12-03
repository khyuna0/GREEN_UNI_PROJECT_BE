package com.green.university.config;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String accesskey;

    private final long ACCESS_TOKEN_EXP = 1000L * 60 * 60; //1시간

    public String createAccessToken(Long userId, String role){
        Date now = new Date();

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role",role)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime()+ACCESS_TOKEN_EXP))


    }


}
