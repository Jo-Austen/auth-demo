package com.example.authdemo.service.impl;

import com.example.authdemo.dto.AssignUserRolesRequest;
import com.example.authdemo.dto.CreateUserRequest;
import com.example.authdemo.dto.RoleSummaryResponse;
import com.example.authdemo.dto.UpdateUserRequest;
import com.example.authdemo.dto.UserDetailResponse;
import com.example.authdemo.dto.UserListResponse;
import com.example.authdemo.entity.Role;
import com.example.authdemo.entity.User;
import com.example.authdemo.entity.UserRole;
import com.example.authdemo.exception.ConflictException;
import com.example.authdemo.exception.NotFoundException;
import com.example.authdemo.repository.RoleRepository;
import com.example.authdemo.repository.UserRepository;
import com.example.authdemo.repository.UserRoleRepository;
import com.example.authdemo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDetailResponse createUser(CreateUserRequest request) {
        validateDuplicateUsername(request.getUsername(), null);

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(request.getEnabled())
                .build();
        User savedUser = userRepository.save(user);

        List<Role> roles = resolveRoles(request.getRoleIds());
        replaceUserRoles(savedUser, roles);
        return buildUserDetailResponse(savedUser, roles);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getUserById(Long id) {
        User user = getUser(id);
        List<Role> roles = findRolesByUserId(user.getId());
        return buildUserDetailResponse(user, roles);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserListResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(user -> UserListResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .enabled(user.getEnabled())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public UserDetailResponse updateUser(Long id, UpdateUserRequest request) {
        User user = getUser(id);
        validateDuplicateUsername(request.getUsername(), id);

        user.setUsername(request.getUsername());
        user.setEnabled(request.getEnabled());
        User savedUser = userRepository.save(user);

        List<Role> roles = findRolesByUserId(savedUser.getId());
        return buildUserDetailResponse(savedUser, roles);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = getUser(id);
        userRoleRepository.deleteAllByIdUserId(user.getId());
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserDetailResponse assignRoles(Long id, AssignUserRolesRequest request) {
        User user = getUser(id);
        List<Role> roles = resolveRoles(request == null ? null : request.getRoleIds());
        replaceUserRoles(user, roles);
        return buildUserDetailResponse(user, roles);
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void validateDuplicateUsername(String username, Long currentUserId) {
        userRepository.findByUsername(username)
                .filter(user -> !user.getId().equals(currentUserId))
                .ifPresent(user -> {
                    throw new ConflictException("Username already exists");
                });
    }

    private List<Role> resolveRoles(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> distinctRoleIds = new ArrayList<>(new LinkedHashSet<>(roleIds));
        List<Role> roles = roleRepository.findAllById(distinctRoleIds);
        if (roles.size() != distinctRoleIds.size()) {
            throw new NotFoundException("One or more roles do not exist");
        }

        Map<Long, Role> roleMap = roles.stream()
                .collect(Collectors.toMap(Role::getId, Function.identity()));

        return distinctRoleIds.stream()
                .map(roleMap::get)
                .toList();
    }

    private void replaceUserRoles(User user, List<Role> roles) {
        userRoleRepository.deleteAllByIdUserId(user.getId());

        if (roles.isEmpty()) {
            return;
        }

        List<UserRole> userRoles = roles.stream()
                .map(role -> new UserRole(user, role))
                .toList();
        userRoleRepository.saveAll(userRoles);
    }

    private List<Role> findRolesByUserId(Long userId) {
        return userRoleRepository.findAllByIdUserId(userId).stream()
                .map(UserRole::getRole)
                .toList();
    }

    private UserDetailResponse buildUserDetailResponse(User user, List<Role> roles) {
        return UserDetailResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .enabled(user.getEnabled())
                .roles(mapRoles(roles))
                .build();
    }

    private List<RoleSummaryResponse> mapRoles(List<Role> roles) {
        Set<Long> seenRoleIds = new LinkedHashSet<>();
        return roles.stream()
                .filter(role -> seenRoleIds.add(role.getId()))
                .map(role -> RoleSummaryResponse.builder()
                        .id(role.getId())
                        .code(role.getCode())
                        .name(role.getName())
                        .build())
                .toList();
    }
}
