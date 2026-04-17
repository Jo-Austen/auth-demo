package com.example.authdemo.contract.user;

import com.example.authdemo.common.constant.PermissionCode;
import com.example.authdemo.entity.User;
import com.example.authdemo.support.base.BaseContractTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.List;

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
    void listUsers_shouldReturnForbidden_whenTokenDoesNotHaveReadPermission() {
        User operator = createUser("contract-user-creator");
        createRoleWithPermissions("CONTRACT_USER_CREATOR", List.of(PermissionCode.USER_CREATE), operator);

        given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(operator))
                .when()
                .get("/users")
                .then()
                .statusCode(403)
                .body("code", equalTo(403))
                .body("message", equalTo("Missing permission: user:read"))
                .body("data", equalTo(null));
    }

    @Test
    void getUserById_shouldReturnForbidden_whenTokenDoesNotHaveReadPermission() {
        User operator = createUser("contract-user-creator");
        User target = createUser("contract-target-user");
        createRoleWithPermissions("CONTRACT_USER_CREATOR", List.of(PermissionCode.USER_CREATE), operator);

        given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(operator))
                .when()
                .get("/users/{id}", target.getId())
                .then()
                .statusCode(403)
                .body("code", equalTo(403))
                .body("message", equalTo("Missing permission: user:read"))
                .body("data", equalTo(null));
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
    void createUser_shouldReturnForbidden_whenTokenDoesNotHaveCreatePermission() {
        User operator = createUser("contract-user-reader");
        createRoleWithPermissions("CONTRACT_USER_READER", List.of(PermissionCode.USER_READ), operator);

        given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(operator))
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "contract-forbidden-user",
                          "password": "password123",
                          "enabled": true
                        }
                        """)
                .when()
                .post("/users")
                .then()
                .statusCode(403)
                .body("code", equalTo(403))
                .body("message", equalTo("Missing permission: user:create"))
                .body("data", equalTo(null));
    }

    @Test
    void updateUser_shouldReturnForbidden_whenTokenDoesNotHaveUpdatePermission() {
        User operator = createUser("contract-user-reader");
        User target = createUser("contract-target-user");
        createRoleWithPermissions("CONTRACT_USER_READER", List.of(PermissionCode.USER_READ), operator);

        given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(operator))
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "contract-updated-user",
                          "enabled": true
                        }
                        """)
                .when()
                .put("/users/{id}", target.getId())
                .then()
                .statusCode(403)
                .body("code", equalTo(403))
                .body("message", equalTo("Missing permission: user:update"))
                .body("data", equalTo(null));
    }

    @Test
    void deleteUser_shouldReturnForbidden_whenTokenDoesNotHaveDeletePermission() {
        User operator = createUser("contract-user-reader");
        User target = createUser("contract-target-user");
        createRoleWithPermissions("CONTRACT_USER_READER", List.of(PermissionCode.USER_READ), operator);

        given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(operator))
                .when()
                .delete("/users/{id}", target.getId())
                .then()
                .statusCode(403)
                .body("code", equalTo(403))
                .body("message", equalTo("Missing permission: user:delete"))
                .body("data", equalTo(null));
    }

    @Test
    void assignRoles_shouldReturnForbidden_whenTokenDoesNotHaveUpdatePermission() {
        User operator = createUser("contract-user-reader");
        User target = createUser("contract-target-user");
        createRoleWithPermissions("CONTRACT_USER_READER", List.of(PermissionCode.USER_READ), operator);

        given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(operator))
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "roleIds": []
                        }
                        """)
                .when()
                .put("/users/{id}/roles", target.getId())
                .then()
                .statusCode(403)
                .body("code", equalTo(403))
                .body("message", equalTo("Missing permission: user:update"))
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
}
