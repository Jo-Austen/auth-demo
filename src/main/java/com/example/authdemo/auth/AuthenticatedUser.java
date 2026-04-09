package com.example.authdemo.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor
public class AuthenticatedUser {

    private final Long userId;
    private final String username;
    @Builder.Default
    private final Set<String> permissions = Collections.emptySet();

    public boolean hasPermission(String permissionCode) {
        return permissions.contains(permissionCode);
    }

    public Set<String> getPermissions() {
        return Collections.unmodifiableSet(new HashSet<>(permissions));
    }
}
