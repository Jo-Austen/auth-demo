package com.example.authdemo.repository;

import com.example.authdemo.entity.RolePermission;
import com.example.authdemo.entity.RolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {

    boolean existsByIdRoleIdAndIdPermissionId(Long roleId, Long permissionId);

    List<RolePermission> findAllByIdRoleId(Long roleId);

    List<RolePermission> findAllByIdPermissionId(Long permissionId);

    void deleteAllByIdRoleId(Long roleId);

    void deleteByIdRoleIdAndIdPermissionId(Long roleId, Long permissionId);
}
