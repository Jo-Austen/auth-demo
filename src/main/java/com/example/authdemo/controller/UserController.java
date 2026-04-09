package com.example.authdemo.controller;

import com.example.authdemo.auth.RequirePermission;
import com.example.authdemo.common.api.ApiResponse;
import com.example.authdemo.common.constant.PermissionCode;
import com.example.authdemo.dto.AssignUserRolesRequest;
import com.example.authdemo.dto.CreateUserRequest;
import com.example.authdemo.dto.UpdateUserRequest;
import com.example.authdemo.dto.UserDetailResponse;
import com.example.authdemo.dto.UserListResponse;
import com.example.authdemo.service.UserService;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @RequirePermission(PermissionCode.USER_CREATE)
    public ApiResponse<UserDetailResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.success(userService.createUser(request));
    }

    @GetMapping("/{id}")
    @RequirePermission(PermissionCode.USER_READ)
    public ApiResponse<UserDetailResponse> getUserById(@PathVariable Long id) {
        return ApiResponse.success(userService.getUserById(id));
    }

    @GetMapping
    @RequirePermission(PermissionCode.USER_READ)
    public ApiResponse<List<UserListResponse>> listUsers() {
        return ApiResponse.success(userService.listUsers());
    }

    @PutMapping("/{id}")
    @RequirePermission(PermissionCode.USER_UPDATE)
    public ApiResponse<UserDetailResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ApiResponse.success(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @RequirePermission(PermissionCode.USER_DELETE)
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/roles")
    @RequirePermission(PermissionCode.USER_UPDATE)
    public ApiResponse<UserDetailResponse> assignRoles(
            @PathVariable Long id,
            @RequestBody AssignUserRolesRequest request
    ) {
        return ApiResponse.success(userService.assignRoles(id, request));
    }
}
