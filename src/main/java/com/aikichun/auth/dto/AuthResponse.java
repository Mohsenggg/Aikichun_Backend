package com.aikichun.auth.dto;

import java.time.Instant;

public record AuthResponse(
        PublicUserDto user,
        String token,
        String tokenType,
        Instant expiresAt
) {
}
