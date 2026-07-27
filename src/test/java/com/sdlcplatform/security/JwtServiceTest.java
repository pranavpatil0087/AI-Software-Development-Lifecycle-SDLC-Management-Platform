package com.sdlcplatform.security;

import com.sdlcplatform.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties();
        appProperties.getJwt().setSecret("test-secret-test-secret-test-secret-test-secret-32chars");
        appProperties.getJwt().setAccessTokenExpirationMs(900000);
        appProperties.getJwt().setRefreshTokenExpirationMs(604800000);

        jwtService = new JwtService(appProperties);

        userDetails = User.builder()
                .username("dev@sdlcplatform.com")
                .password("irrelevant")
                .authorities("ROLE_DEVELOPER")
                .build();
    }

    @Test
    void generatedToken_shouldContainCorrectUsername() {
        String token = jwtService.generateAccessToken(userDetails);
        assertThat(jwtService.extractUsername(token)).isEqualTo("dev@sdlcplatform.com");
    }

    @Test
    void generatedToken_shouldBeValidForMatchingUser() {
        String token = jwtService.generateAccessToken(userDetails);
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void token_shouldBeInvalid_forDifferentUser() {
        String token = jwtService.generateAccessToken(userDetails);

        UserDetails otherUser = User.builder()
                .username("someone-else@sdlcplatform.com")
                .password("irrelevant")
                .authorities("ROLE_TESTER")
                .build();

        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    void tamperedToken_shouldFailValidation() {
        String token = jwtService.generateAccessToken(userDetails);
        String tampered = token.substring(0, token.length() - 5) + "abcde";

        assertThat(jwtService.isTokenValid(tampered, userDetails)).isFalse();
    }
}
