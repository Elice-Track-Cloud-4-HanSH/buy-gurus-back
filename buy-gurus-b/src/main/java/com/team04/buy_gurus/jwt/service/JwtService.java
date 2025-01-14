package com.team04.buy_gurus.jwt.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.team04.buy_gurus.exception.ex_user.ex.UserNotFoundException;
import com.team04.buy_gurus.jwt.JwtProperties;
import com.team04.buy_gurus.user.entity.User;
import com.team04.buy_gurus.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
@Getter
@RequiredArgsConstructor
@Transactional
public class JwtService {

    private final JwtProperties jwtProperties;

    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String ACCESS_TOKEN_COOKIE = "accessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final String USER_ID_CLAIM = "user_id";
    private static final String ROLE_CLAIM = "role";
    private static final String BEARER = "Bearer ";

    private final UserRepository userRepository;

    public String createAccessToken(Long userId, String role) {
        Date now = new Date();
        return JWT.create()
                .withSubject(userId.toString())
                .withExpiresAt(new Date(now.getTime() + jwtProperties.getAccessTokenExpiration()))
                .withClaim(ROLE_CLAIM, role)
                .sign(Algorithm.HMAC512(jwtProperties.getSecretKey()));
    }

    public Optional<String> extractAccessToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(ACCESS_TOKEN_COOKIE)) {
                    return Optional.of(cookie.getValue());
                }
            }
        }
        return Optional.empty();
    }

    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(REFRESH_TOKEN_COOKIE)) {
                    return Optional.of(cookie.getValue());
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Long> extractUserId(String accessToken) {
        try {
            String userId = JWT.require(Algorithm.HMAC512(jwtProperties.getSecretKey()))
                    .build()
                    .verify(accessToken)
                    .getSubject();

            return Optional.ofNullable(Long.valueOf(userId));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<String> extractRole(String accessToken) {
        try {
            return Optional.ofNullable(JWT.require(Algorithm.HMAC512(jwtProperties.getSecretKey()))
                    .build()
                    .verify(accessToken)
                    .getClaim(ROLE_CLAIM)
                    .asString());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public boolean isTokenValid(String token) {
        try {
            JWT.require(Algorithm.HMAC512(jwtProperties.getSecretKey())).build().verify(token);
            return true;
        } catch (JWTVerificationException e) {
            // log.error("토큰 검증 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            // log.error("예상치 못한 오류: " + e.getMessage(), e);
        }
        return false;
    }

    public void addAccessTokenToCookie(HttpServletResponse response, String accessToken) {
        Cookie accessTokenCookie = new Cookie(ACCESS_TOKEN_COOKIE, accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(60 * 60);
        response.addCookie(accessTokenCookie);
    }

    public void addRefreshTokenToCookie(HttpServletResponse response, String refreshToken) {
        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(refreshTokenCookie);
    }

    public void removeAccessTokenToCookie(HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie(ACCESS_TOKEN_COOKIE, null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);
        response.addCookie(accessTokenCookie);
    }

    public void removeRefreshTokenToCookie(HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE, null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        response.addCookie(refreshTokenCookie);
    }
}
