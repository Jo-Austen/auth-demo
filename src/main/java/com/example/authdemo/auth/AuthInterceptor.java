package com.example.authdemo.auth;

import com.example.authdemo.entity.User;
import com.example.authdemo.exception.UnauthenticatedException;
import com.example.authdemo.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new UnauthenticatedException("Authentication token is required");
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw new UnauthenticatedException("Authentication token is required");
        }

        AuthenticatedUser tokenUser = jwtService.parseToken(token);
        User user = userRepository.findById(tokenUser.getUserId())
                .filter(User::getEnabled)
                .orElseThrow(() -> new UnauthenticatedException("User does not exist or is disabled"));

        CurrentUserContext.set(AuthenticatedUser.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .build());
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        CurrentUserContext.clear();
    }
}
