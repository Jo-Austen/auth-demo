package com.example.authdemo.dto;

import lombok.Data;

import java.util.List;

@Data
public class AssignUserRolesRequest {

    private List<Long> roleIds;
}
