package com.example.authdemo.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthenticatedUser {

    private final Long userId;
    private final String username;
}
