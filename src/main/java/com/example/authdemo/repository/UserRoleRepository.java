package com.example.authdemo.repository;

import com.example.authdemo.entity.UserRole;
import com.example.authdemo.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    boolean existsByIdUserIdAndIdRoleId(Long userId, Long roleId);

    List<UserRole> findAllByIdUserId(Long userId);

    List<UserRole> findAllByIdRoleId(Long roleId);

    void deleteAllByIdUserId(Long userId);

    void deleteAllByIdRoleId(Long roleId);

    void deleteByIdUserIdAndIdRoleId(Long userId, Long roleId);
}
