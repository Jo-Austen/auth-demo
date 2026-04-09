package com.example.authdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDetailResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private List<PermissionSummaryResponse> permissions;
}
