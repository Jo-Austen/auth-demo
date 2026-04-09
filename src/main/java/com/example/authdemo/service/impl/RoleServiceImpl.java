package com.example.authdemo.service.impl;

import com.example.authdemo.dto.AssignRolePermissionsRequest;
import com.example.authdemo.dto.CreateRoleRequest;
import com.example.authdemo.dto.PermissionSummaryResponse;
import com.example.authdemo.dto.RoleDetailResponse;
import com.example.authdemo.dto.RoleListResponse;
import com.example.authdemo.dto.UpdateRoleRequest;
import com.example.authdemo.entity.Permission;
import com.example.authdemo.entity.Role;
import com.example.authdemo.entity.RolePermission;
import com.example.authdemo.exception.ConflictException;
import com.example.authdemo.exception.NotFoundException;
import com.example.authdemo.repository.PermissionRepository;
import com.example.authdemo.repository.RolePermissionRepository;
import com.example.authdemo.repository.RoleRepository;
import com.example.authdemo.repository.UserRoleRepository;
import com.example.authdemo.service.RoleService;
import lombok.RequiredArgsConstructor;
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
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    @Transactional
    public RoleDetailResponse createRole(CreateRoleRequest request) {
        if (roleRepository.existsByCode(request.getCode())) {
            throw new ConflictException("Role code already exists");
        }

        Role role = Role.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .build();
        Role savedRole = roleRepository.save(role);

        List<Permission> permissions = resolvePermissions(request.getPermissionIds());
        replaceRolePermissions(savedRole, permissions);
        return buildRoleDetailResponse(savedRole, permissions);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDetailResponse getRoleById(Long id) {
        Role role = getRole(id);
        List<Permission> permissions = findPermissionsByRoleId(role.getId());
        return buildRoleDetailResponse(role, permissions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleListResponse> listRoles() {
        return roleRepository.findAll().stream()
                .map(role -> RoleListResponse.builder()
                        .id(role.getId())
                        .code(role.getCode())
                        .name(role.getName())
                        .description(role.getDescription())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public RoleDetailResponse updateRole(Long id, UpdateRoleRequest request) {
        Role role = getRole(id);
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        Role savedRole = roleRepository.save(role);

        List<Permission> permissions = findPermissionsByRoleId(savedRole.getId());
        return buildRoleDetailResponse(savedRole, permissions);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = getRole(id);
        userRoleRepository.deleteAllByIdRoleId(role.getId());
        rolePermissionRepository.deleteAllByIdRoleId(role.getId());
        roleRepository.delete(role);
    }

    @Override
    @Transactional
    public RoleDetailResponse assignPermissions(Long id, AssignRolePermissionsRequest request) {
        Role role = getRole(id);
        List<Permission> permissions = resolvePermissions(request == null ? null : request.getPermissionIds());
        replaceRolePermissions(role, permissions);
        return buildRoleDetailResponse(role, permissions);
    }

    private Role getRole(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role not found"));
    }

    private List<Permission> resolvePermissions(List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> distinctPermissionIds = new ArrayList<>(new LinkedHashSet<>(permissionIds));
        List<Permission> permissions = permissionRepository.findAllById(distinctPermissionIds);
        if (permissions.size() != distinctPermissionIds.size()) {
            throw new NotFoundException("One or more permissions do not exist");
        }

        Map<Long, Permission> permissionMap = permissions.stream()
                .collect(Collectors.toMap(Permission::getId, Function.identity()));

        return distinctPermissionIds.stream()
                .map(permissionMap::get)
                .toList();
    }

    private void replaceRolePermissions(Role role, List<Permission> permissions) {
        rolePermissionRepository.deleteAllByIdRoleId(role.getId());

        if (permissions.isEmpty()) {
            return;
        }

        List<RolePermission> rolePermissions = permissions.stream()
                .map(permission -> new RolePermission(role, permission))
                .toList();
        rolePermissionRepository.saveAll(rolePermissions);
    }

    private List<Permission> findPermissionsByRoleId(Long roleId) {
        return rolePermissionRepository.findAllByIdRoleId(roleId).stream()
                .map(RolePermission::getPermission)
                .toList();
    }

    private RoleDetailResponse buildRoleDetailResponse(Role role, List<Permission> permissions) {
        return RoleDetailResponse.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(mapPermissions(permissions))
                .build();
    }

    private List<PermissionSummaryResponse> mapPermissions(List<Permission> permissions) {
        Set<Long> seenPermissionIds = new LinkedHashSet<>();
        return permissions.stream()
                .filter(permission -> seenPermissionIds.add(permission.getId()))
                .map(permission -> PermissionSummaryResponse.builder()
                        .id(permission.getId())
                        .code(permission.getCode())
                        .name(permission.getName())
                        .description(permission.getDescription())
                        .build())
                .toList();
    }
}
