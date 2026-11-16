package com.raghu.fta.config.security;

import com.raghu.fta.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenUtilTest {
    private final JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();

    @Test
    void generateAndValidateTokenWorks() {
        User user = new User();
        user.setEmail("token-user@test.com");

        String token = jwtTokenUtil.generateToken(user);

        assertEquals("token-user@test.com", jwtTokenUtil.getUserEmailFromToken(token));
        assertNotNull(jwtTokenUtil.getExpirationDateFromToken(token));

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "token-user@test.com",
                "secret",
                Collections.emptyList()
        );

        assertTrue(jwtTokenUtil.validateToken(token, userDetails));
    }

    @Test
    void validateTokenRejectsExpiredToken() {
        String expiredToken = Jwts.builder()
                .setSubject("expired@test.com")
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(SignatureAlgorithm.HS256, com.raghu.fta.config.Constants.SIGNING_KEY)
                .compact();

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "expired@test.com",
                "secret",
                Collections.emptyList()
        );

        assertFalse(jwtTokenUtil.validateToken(expiredToken, userDetails));
    }
}
