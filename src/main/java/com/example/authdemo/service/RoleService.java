package com.example.authdemo.service;

import com.example.authdemo.dto.AssignRolePermissionsRequest;
import com.example.authdemo.dto.CreateRoleRequest;
import com.example.authdemo.dto.RoleDetailResponse;
import com.example.authdemo.dto.RoleListResponse;
import com.example.authdemo.dto.UpdateRoleRequest;

import java.util.List;

public interface RoleService {

    RoleDetailResponse createRole(CreateRoleRequest request);

    RoleDetailResponse getRoleById(Long id);

    List<RoleListResponse> listRoles();

    RoleDetailResponse updateRole(Long id, UpdateRoleRequest request);

    void deleteRole(Long id);

    RoleDetailResponse assignPermissions(Long id, AssignRolePermissionsRequest request);
}
