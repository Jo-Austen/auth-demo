package com.example.authdemo.auth;

public final class CurrentUserContext {

    private static final ThreadLocal<AuthenticatedUser> CURRENT_USER = new ThreadLocal<>();

    private CurrentUserContext() {
    }

    public static void set(AuthenticatedUser authenticatedUser) {
        CURRENT_USER.set(authenticatedUser);
    }

    public static AuthenticatedUser get() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
