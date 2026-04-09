package com.example.authdemo.controller;

import com.example.authdemo.auth.RequirePermission;
import com.example.authdemo.common.api.ApiResponse;
import com.example.authdemo.common.constant.PermissionCode;
import com.example.authdemo.dto.AssignRolePermissionsRequest;
import com.example.authdemo.dto.CreateRoleRequest;
import com.example.authdemo.dto.RoleDetailResponse;
import com.example.authdemo.dto.RoleListResponse;
import com.example.authdemo.dto.UpdateRoleRequest;
import com.example.authdemo.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @RequirePermission(PermissionCode.ROLE_CREATE)
    public ApiResponse<RoleDetailResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        return ApiResponse.success(roleService.createRole(request));
    }

    @GetMapping("/{id}")
    @RequirePermission(PermissionCode.ROLE_READ)
    public ApiResponse<RoleDetailResponse> getRoleById(@PathVariable Long id) {
        return ApiResponse.success(roleService.getRoleById(id));
    }

    @GetMapping
    @RequirePermission(PermissionCode.ROLE_READ)
    public ApiResponse<List<RoleListResponse>> listRoles() {
        return ApiResponse.success(roleService.listRoles());
    }

    @PutMapping("/{id}")
    @RequirePermission(PermissionCode.ROLE_UPDATE)
    public ApiResponse<RoleDetailResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        return ApiResponse.success(roleService.updateRole(id, request));
    }

    @DeleteMapping("/{id}")
    @RequirePermission(PermissionCode.ROLE_DELETE)
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/permissions")
    @RequirePermission(PermissionCode.ROLE_UPDATE)
    public ApiResponse<RoleDetailResponse> assignPermissions(
            @PathVariable Long id,
            @RequestBody AssignRolePermissionsRequest request
    ) {
        return ApiResponse.success(roleService.assignPermissions(id, request));
    }
}
