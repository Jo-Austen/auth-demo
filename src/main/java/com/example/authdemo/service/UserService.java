package com.example.authdemo.service;

import com.example.authdemo.dto.AssignUserRolesRequest;
import com.example.authdemo.dto.CreateUserRequest;
import com.example.authdemo.dto.UpdateUserRequest;
import com.example.authdemo.dto.UserDetailResponse;
import com.example.authdemo.dto.UserListResponse;

import java.util.List;

public interface UserService {

    UserDetailResponse createUser(CreateUserRequest request);

    UserDetailResponse getUserById(Long id);

    List<UserListResponse> listUsers();

    UserDetailResponse updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);

    UserDetailResponse assignRoles(Long id, AssignUserRolesRequest request);
}
