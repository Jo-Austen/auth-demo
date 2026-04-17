package com.example.authdemo.contract.auth;

import com.example.authdemo.entity.User;
import com.example.authdemo.support.base.BaseContractTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

// Contract tests for auth API request and response shape.
class AuthApiContractTest extends BaseContractTest {

    @Test
    void login_shouldReturnTokenAndUserInfo_whenCredentialsAreValid() {
        User user = createUser("contract-login-user", "password123", true);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "contract-login-user",
                          "password": "password123"
                        }
                        """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("code", equalTo(0))
                .body("message", equalTo("Success"))
                .body("data.token", notNullValue())
                .body("data.userId", equalTo(user.getId().intValue()))
                .body("data.username", equalTo("contract-login-user"));
    }

    @Test
    void login_shouldReturnValidationError_whenRequestBodyIsInvalid() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "username": "contract-login-user"
                        }
                        """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(400)
                .body("code", equalTo(400))
                .body("message", equalTo("Validation failed"))
                .body("data", equalTo(null));
    }
}
