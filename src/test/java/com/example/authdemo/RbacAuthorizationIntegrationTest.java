package com.example.authdemo;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RbacAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        rolePermissionRepository.deleteAll();
        userRoleRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldRejectRequestWithoutPermission() throws Exception {
        User user = createUser("reader");
        createRoleWithPermissions("USER_READER", List.of(PermissionCode.USER_READ), user);

        mockMvc.perform(post("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "new-user",
                                  "password": "password123",
                                  "enabled": true
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("Missing permission: user:create"));
    }

    @Test
    void shouldAllowRequestWithPermission() throws Exception {
        User user = createUser("admin-user");
        createRoleWithPermissions("USER_ADMIN", List.of(PermissionCode.USER_CREATE), user);

        mockMvc.perform(post("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "created-user",
                                  "password": "password123",
                                  "enabled": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.username").value("created-user"));
    }

    @Test
    void shouldProtectRolePermissionAssignmentEndpoint() throws Exception {
        User user = createUser("role-reader");
        Role role = roleRepository.save(Role.builder()
                .code("AUDITOR")
                .name("Auditor")
                .description("Read only role")
                .build());
        Permission permission = createPermission(PermissionCode.ROLE_READ);
        createRoleWithPermissions("ROLE_READER", List.of(PermissionCode.ROLE_READ), user);

        mockMvc.perform(put("/api/roles/{id}/permissions", role.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "permissionIds": [%d]
                                }
                                """.formatted(permission.getId())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("Missing permission: role:update"));
    }

    @Test
    void shouldRequireAuthenticationForProtectedEndpoints() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void shouldAllowDeleteWhenUserHasDeletePermission() throws Exception {
        User operator = createUser("user-deleter");
        User victim = createUser("victim");
        createRoleWithPermissions("USER_DELETER", List.of(PermissionCode.USER_DELETE), operator);

        mockMvc.perform(delete("/api/users/{id}", victim.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(operator)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    private User createUser(String username) {
        return userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode("password123"))
                .enabled(true)
                .build());
    }

    private Role createRoleWithPermissions(String roleCode, List<PermissionCode> permissionCodes, User user) {
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

    private Permission createPermission(PermissionCode permissionCode) {
        return permissionRepository.findByCode(permissionCode.getCode())
                .orElseGet(() -> permissionRepository.save(Permission.builder()
                        .code(permissionCode.getCode())
                        .name(permissionCode.name())
                        .description(permissionCode.getCode())
                        .build()));
    }

    private String bearerToken(User user) {
        return "Bearer " + jwtService.generateToken(user);
    }
}
