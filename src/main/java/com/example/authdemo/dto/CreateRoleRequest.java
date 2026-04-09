package com.example.authdemo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateRoleRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String description;

    private List<Long> permissionIds;
}
