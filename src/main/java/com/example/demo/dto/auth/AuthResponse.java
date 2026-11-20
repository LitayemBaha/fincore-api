package com.example.demo.dto.auth;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresInMs,
        Long userId,
        String name,
        String email
) {
}
