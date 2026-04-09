package com.example.authdemo.config;

import com.example.authdemo.common.constant.PermissionCode;
import com.example.authdemo.entity.Permission;
import com.example.authdemo.entity.Role;
import com.example.authdemo.entity.RolePermission;
import com.example.authdemo.entity.User;
import com.example.authdemo.entity.UserRole;
import com.example.authdemo.repository.PermissionRepository;
import com.example.authdemo.repository.RolePermissionRepository;
import com.example.authdemo.repository.RoleRepository;
import com.example.authdemo.repository.UserRepository;
import com.example.authdemo.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PermissionDataInitializer implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        Arrays.stream(PermissionCode.values()).forEach(this::createPermissionIfMissing);
        Map<String, Permission> permissionsByCode = permissionRepository.findAll().stream()
                .collect(Collectors.toMap(Permission::getCode, Function.identity()));

        Role adminRole = createRoleIfMissing("ADMIN", "Administrator", "Full access to all user and role operations");
        Role userManagerRole = createRoleIfMissing("USER_MANAGER", "User Manager", "Manage users");
        Role roleManagerRole = createRoleIfMissing("ROLE_MANAGER", "Role Manager", "Manage roles");

        assignPermissionsIfMissing(adminRole, permissionsByCode.values().stream().toList());
        assignPermissionsIfMissing(userManagerRole, findPermissionsByPrefix(permissionsByCode, "user:"));
        assignPermissionsIfMissing(roleManagerRole, findPermissionsByPrefix(permissionsByCode, "role:"));

        User adminUser = createAdminUserIfMissing();
        assignRoleIfMissing(adminUser, adminRole);
    }

    private void createPermissionIfMissing(PermissionCode permissionCode) {
        permissionRepository.findByCode(permissionCode.getCode())
                .orElseGet(() -> permissionRepository.save(
                        Permission.builder()
                                .code(permissionCode.getCode())
                                .name(toDisplayName(permissionCode.getCode()))
                                .description("Baseline permission: " + permissionCode.getCode())
                                .build()
                ));
    }

    private Role createRoleIfMissing(String code, String name, String description) {
        return roleRepository.findByCode(code)
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .code(code)
                                .name(name)
                                .description(description)
                                .build()
                ));
    }

    private User createAdminUserIfMissing() {
        return userRepository.findByUsername("admin")
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .username("admin")
                                .password(passwordEncoder.encode("admin123"))
                                .enabled(true)
                                .build()
                ));
    }

    private void assignPermissionsIfMissing(Role role, List<Permission> permissions) {
        permissions.forEach(permission -> {
            if (!rolePermissionRepository.existsByIdRoleIdAndIdPermissionId(role.getId(), permission.getId())) {
                rolePermissionRepository.save(new RolePermission(role, permission));
            }
        });
    }

    private void assignRoleIfMissing(User user, Role role) {
        if (!userRoleRepository.existsByIdUserIdAndIdRoleId(user.getId(), role.getId())) {
            userRoleRepository.save(new UserRole(user, role));
        }
    }

    private List<Permission> findPermissionsByPrefix(Map<String, Permission> permissionsByCode, String prefix) {
        return permissionsByCode.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(prefix))
                .map(Map.Entry::getValue)
                .toList();
    }

    private String toDisplayName(String code) {
        String[] parts = code.split(":");
        return capitalize(parts[0]) + " " + capitalize(parts[1]);
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }
}
