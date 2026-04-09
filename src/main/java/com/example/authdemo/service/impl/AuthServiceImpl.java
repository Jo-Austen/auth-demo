package com.example.authdemo.service.impl;

import com.example.authdemo.auth.JwtService;
import com.example.authdemo.dto.LoginRequest;
import com.example.authdemo.dto.LoginResponse;
import com.example.authdemo.entity.User;
import com.example.authdemo.exception.UnauthenticatedException;
import com.example.authdemo.repository.UserRepository;
import com.example.authdemo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .filter(User::getEnabled)
                .orElseThrow(() -> new UnauthenticatedException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthenticatedException("Invalid username or password");
        }

        return LoginResponse.builder()
                .token(jwtService.generateToken(user))
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }
}
