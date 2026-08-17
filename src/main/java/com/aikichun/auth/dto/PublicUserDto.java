package com.aikichun.auth.dto;

import com.aikichun.user.entites.Role;
import com.aikichun.user.entites.User;

import java.util.UUID;

public record PublicUserDto(
        UUID id,
        String email,
        String fullName,
        Role role
) {
    public static PublicUserDto from(User user) {
        return new PublicUserDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole()
        );
    }
}
