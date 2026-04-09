package com.example.authdemo.common.constant;

import lombok.Getter;

@Getter
public enum PermissionCode {

    USER_CREATE("user:create"),
    USER_READ("user:read"),
    USER_UPDATE("user:update"),
    USER_DELETE("user:delete"),
    ROLE_CREATE("role:create"),
    ROLE_READ("role:read"),
    ROLE_UPDATE("role:update"),
    ROLE_DELETE("role:delete");

    private final String code;

    PermissionCode(String code) {
        this.code = code;
    }
}
