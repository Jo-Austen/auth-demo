package com.example.authdemo.unit.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.authdemo.auth.JwtService;
import com.example.authdemo.dto.LoginRequest;
import com.example.authdemo.dto.LoginResponse;
import com.example.authdemo.entity.User;
import com.example.authdemo.exception.UnauthenticatedException;
import com.example.authdemo.repository.UserRepository;
import com.example.authdemo.service.impl.AuthServiceImpl;

// Unit tests for auth service login behavior.
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;  

    @InjectMocks
    private AuthServiceImpl authService;   

    @Test
    void login_shouldReturnTokenAndUserInfo_whenUsernameAndPasswordAreCorrect() {
        // Given
        LoginRequest request = validLoginRequest();
        User user = enabledUser();

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("mocked-jwt-token");

        // When
        LoginResponse response = authService.login(request);

        // Then
        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.getToken());
        assertEquals(user.getId(), response.getUserId());
        assertEquals(user.getUsername(), response.getUsername());
    }

    @Test
    void login_shouldThrowException_whenUserDoesNotExist() {
        // Given
        LoginRequest request = validLoginRequest();

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UnauthenticatedException.class, () -> authService.login(request));

    }

    @Test
    void login_shouldThrowException_whenUserIsDisabled() {
        // Given
        LoginRequest request = validLoginRequest();
        User user = disabledUser();

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));

        // When & Then
        assertThrows(UnauthenticatedException.class, () -> authService.login(request));
    }

    @Test
    void login_shouldThrowException_whenPasswordIsWrong() {
        // Given
        LoginRequest request = validLoginRequest();
        User user = enabledUser();

        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        // When & Then
        assertThrows(UnauthenticatedException.class, () -> authService.login(request));
    }
    

    private LoginRequest validLoginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("110110");
        return request;
    }

    private User enabledUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword("encoded-password");
        user.setEnabled(true);
        return user;
    }

    private User disabledUser() {
        User user = enabledUser();
        user.setEnabled(false);
        return user;
    }

}
