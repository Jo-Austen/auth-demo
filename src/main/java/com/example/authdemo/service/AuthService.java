package com.example.authdemo.service;

import com.example.authdemo.dto.LoginRequest;
import com.example.authdemo.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}
