package com.example.authdemo.auth;

import com.example.authdemo.exception.ForbiddenException;
import com.example.authdemo.exception.UnauthenticatedException;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

@Component
public class PermissionGuard {

    public void checkPermission(HandlerMethod handlerMethod) {
        RequirePermission requirePermission = findRequiredPermission(handlerMethod);
        if (requirePermission == null) {
            return;
        }

        AuthenticatedUser authenticatedUser = CurrentUserContext.get();
        if (authenticatedUser == null) {
            throw new UnauthenticatedException("Authentication is required");
        }

        String permissionCode = requirePermission.value().getCode();
        if (!authenticatedUser.hasPermission(permissionCode)) {
            throw new ForbiddenException("Missing permission: " + permissionCode);
        }
    }

    private RequirePermission findRequiredPermission(HandlerMethod handlerMethod) {
        RequirePermission methodAnnotation = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        return handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
    }
}
