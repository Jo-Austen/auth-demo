package com.example.authdemo.repository;

import com.example.authdemo.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByCode(String code);

    boolean existsByCode(String code);

    @Query("""
            select distinct permission.code
            from UserRole userRole
            join userRole.role role
            join role.rolePermissions rolePermission
            join rolePermission.permission permission
            where userRole.user.id = :userId
            """)
    Set<String> findPermissionCodesByUserId(Long userId);
}
