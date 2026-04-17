package com.example.authdemo.contract.user;

import com.example.authdemo.common.constant.PermissionCode;
import com.example.authdemo.entity.User;
import com.example.authdemo.support.base.BaseContractTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

// Contract tests for user API request and response shape.
class UserApiContractTest extends BaseContractTest {

    @Test
    void listUsers_shouldReturnUnauthorized_whenTokenIsMissing() {
        given()
                .when()
                .get("/users")
                .then()
                .statusCode(401)
                .body("code", equalTo(401))
                .body("message", notNullValue())
                .body("data", equalTo(null));
    }

    @Test
    void listUsers_shouldReturnUsers_whenTokenHasReadPermission() {
        User operator = createUser("contract-user-reader");
        createUser("contract-visible-user");
        createRoleWithPermissions("CONTRACT_USER_READER", List.of(PermissionCode.USER_READ), operator);

        given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(operator))
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .body("code", equalTo(0))
                .body("message", equalTo("Success"))
                .body("data", notNullValue())
                .body("data.username", hasItem("contract-visible-user"));
    }

    @Test
    void createUser_shouldReturnValidationError_whenRequestBodyIsInvalid() {
        User operator = createUser("contract-user-creator");
        createRoleWithPermissions("CONTRACT_USER_CREATOR", List.of(PermissionCode.USER_CREATE), operator);

        given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(operator))
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .post("/users")
                .then()
                .statusCode(400)
                .body("code", equalTo(400))
                .body("message", equalTo("Validation failed"))
                .body("data", equalTo(null));
    }

    @Test
    void createUser_shouldReturnCreatedUser_whenTokenHasCreatePermission() {
        User operator = createUser("contract-user-creator");
        createRoleWithPermissions("CONTRACT_USER_CREATOR", List.of(PermissionCode.USER_CREATE), operator);

        given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(operator))
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "contract-created-user",
                          "password": "password123",
                          "enabled": true
                        }
                        """)
                .when()
                .post("/users")
                .then()
                .statusCode(200)
                .body("code", equalTo(0))
                .body("message", equalTo("Success"))
                .body("data.id", notNullValue())
                .body("data.username", equalTo("contract-created-user"))
                .body("data.enabled", equalTo(true));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("forbiddenUserApiCases")
    void userApi_shouldReturnForbidden_whenTokenDoesNotHaveRequiredPermission(ForbiddenUserApiCase testCase) {
        User operator = createUser("contract-forbidden-operator");
        User target = createUser("contract-target-user");
        createRoleWithPermissions("CONTRACT_GRANTED_ROLE", List.of(testCase.grantedPermission()), operator);

        RequestSpecification request = given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(operator));
        if (testCase.requestBody() != null) {
            request.contentType(ContentType.JSON).body(testCase.requestBody());
        }

        Object[] pathParams = testCase.requiresTargetUser() ? new Object[]{target.getId()} : new Object[0];
        request
                .when()
                .request(testCase.method(), testCase.path(), pathParams)
                .then()
                .statusCode(403)
                .body("code", equalTo(403))
                .body("message", equalTo(testCase.expectedMessage()))
                .body("data", equalTo(null));
    }

    private static Stream<ForbiddenUserApiCase> forbiddenUserApiCases() {
        return Stream.of(
                new ForbiddenUserApiCase(
                        "list users requires user:read",
                        "GET",
                        "/users",
                        false,
                        null,
                        PermissionCode.USER_CREATE,
                        "Missing permission: user:read"
                ),
                new ForbiddenUserApiCase(
                        "get user by id requires user:read",
                        "GET",
                        "/users/{id}",
                        true,
                        null,
                        PermissionCode.USER_CREATE,
                        "Missing permission: user:read"
                ),
                new ForbiddenUserApiCase(
                        "create user requires user:create",
                        "POST",
                        "/users",
                        false,
                        """
                                {
                                  "username": "contract-forbidden-user",
                                  "password": "password123",
                                  "enabled": true
                                }
                                """,
                        PermissionCode.USER_READ,
                        "Missing permission: user:create"
                ),
                new ForbiddenUserApiCase(
                        "update user requires user:update",
                        "PUT",
                        "/users/{id}",
                        true,
                        """
                                {
                                  "username": "contract-updated-user",
                                  "enabled": true
                                }
                                """,
                        PermissionCode.USER_READ,
                        "Missing permission: user:update"
                ),
                new ForbiddenUserApiCase(
                        "delete user requires user:delete",
                        "DELETE",
                        "/users/{id}",
                        true,
                        null,
                        PermissionCode.USER_READ,
                        "Missing permission: user:delete"
                ),
                new ForbiddenUserApiCase(
                        "assign roles requires user:update",
                        "PUT",
                        "/users/{id}/roles",
                        true,
                        """
                                {
                                  "roleIds": []
                                }
                                """,
                        PermissionCode.USER_READ,
                        "Missing permission: user:update"
                )
        );
    }

    private record ForbiddenUserApiCase(
            String name,
            String method,
            String path,
            boolean requiresTargetUser,
            String requestBody,
            PermissionCode grantedPermission,
            String expectedMessage
    ) {

        @Override
        public String toString() {
            return name;
        }
    }
}
