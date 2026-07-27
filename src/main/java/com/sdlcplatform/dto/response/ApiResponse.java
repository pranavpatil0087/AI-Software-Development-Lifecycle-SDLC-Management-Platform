package com.sdlcplatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Generic success-envelope for endpoints that just need to confirm an action
 * (e.g. "verification email sent") without returning a domain object.
 */
@Getter
@Builder
@AllArgsConstructor
public class ApiResponse {
    private boolean success;
    private String message;
    private Instant timestamp;

    public static ApiResponse of(String message) {
        return ApiResponse.builder()
                .success(true)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }
}
