package com.example.authdemo.support.base;

import com.example.authdemo.auth.JwtService;
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
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseContractTest {

    @LocalServerPort
    private int port;

    @Autowired
    protected JwtService jwtService;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RoleRepository roleRepository;

    @Autowired
    protected PermissionRepository permissionRepository;

    @Autowired
    protected UserRoleRepository userRoleRepository;

    @Autowired
    protected RolePermissionRepository rolePermissionRepository;

    @Autowired
    protected BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUpContractTest() {
        cleanDatabase();
        RestAssured.port = port;
        RestAssured.basePath = "/api";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    protected User createUser(String username) {
        return createUser(username, "password123", true);
    }

    protected User createUser(String username, String password, boolean enabled) {
        return userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .enabled(enabled)
                .build());
    }

    protected Role createRoleWithPermissions(String roleCode, List<PermissionCode> permissionCodes, User user) {
        Role role = roleRepository.save(Role.builder()
                .code(roleCode)
                .name(roleCode)
                .description(roleCode)
                .build());

        List<Permission> permissions = permissionCodes.stream()
                .map(this::createPermission)
                .toList();

        rolePermissionRepository.saveAll(permissions.stream()
                .map(permission -> new RolePermission(role, permission))
                .toList());
        userRoleRepository.save(new UserRole(user, role));
        return role;
    }

    protected String bearerToken(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }

    private Permission createPermission(PermissionCode permissionCode) {
        return permissionRepository.findByCode(permissionCode.getCode())
                .orElseGet(() -> permissionRepository.save(Permission.builder()
                        .code(permissionCode.getCode())
                        .name(permissionCode.name())
                        .description(permissionCode.getCode())
                        .build()));
    }

    private void cleanDatabase() {
        rolePermissionRepository.deleteAll();
        userRoleRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
        userRepository.deleteAll();
    }
}
