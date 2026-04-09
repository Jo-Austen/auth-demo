# Auth Demo Backend

A Spring Boot 3 backend demo implementing **username/password authentication**, **JWT-based authorization**, and a simple **RBAC (Role-Based Access Control)** system — built **without Spring Security**.

---

## 🚀 Project Overview

This project provides:

* Secure login with **BCrypt password hashing**
* **JWT-based authentication** for protected APIs
* User management APIs
* Role & permission management (RBAC)
* Seeded baseline data (roles & permissions)
* Unified API response format
* Centralized exception handling

> Authentication is implemented using a **custom interceptor + JWT service**.

---

## 🔐 Security Notice

* ❌ **Do NOT commit real credentials** (database, JWT secret, etc.)
* ✅ Use **environment variables** or **local config files** (ignored by Git)
* ✅ This project contains **example values only**

---

## 🧰 Tech Stack

* Java 17
* Spring Boot 3.3
* Spring Web
* Spring Data JPA
* Bean Validation
* MySQL
* JJWT
* BCrypt (`spring-security-crypto`)
* Lombok

---

## 📦 Module Structure

| Module        | Description                                |
| ------------- | ------------------------------------------ |
| `auth`        | Login, JWT generation & validation         |
| `users`       | User CRUD + role assignment                |
| `roles`       | Role CRUD + permission assignment          |
| `permissions` | Permission definitions (seeded on startup) |

---

## 🗄️ Database Setup

Create a database manually (example):

```sql
CREATE DATABASE auth_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

> Database user and password should be configured locally and **must not be committed**.

---

## ⚙️ Configuration

### Option 1: Environment Variables (Recommended)

```bash
export DB_URL=jdbc:mysql://localhost:3306/auth_demo
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
export JWT_SECRET=your_jwt_secret
```

---

### Option 2: Local Config Override

Create a local file:

```bash
src/main/resources/application-local.yml
```

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

jwt:
  secret: ${JWT_SECRET}
```

> Add this file to `.gitignore` to prevent leaks.

---

### Default `application.yml`

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/auth_demo}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}

jwt:
  secret: ${JWT_SECRET:change_me}
```

---

## ▶️ Run the Project

```bash
mvn spring-boot:run
```

Application runs at:

```
http://localhost:8080
```

---

## 🌱 Seed Data

On startup, the system initializes:

### Permissions

* `user:create`
* `user:read`
* `user:update`
* `user:delete`
* `role:create`
* `role:read`
* `role:update`
* `role:delete`

### Roles

* `ADMIN`
* `USER_MANAGER`
* `ROLE_MANAGER`

### Role Mapping

* `ADMIN` → all permissions
* `USER_MANAGER` → `user:*`
* `ROLE_MANAGER` → `role:*`

---

## 👤 Initial Access

For security reasons:

* ❌ No default password is exposed in this repository
* ✅ You should **create your own admin user manually** or via API

---

## 🔑 Authentication

### Login

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "username": "your_username",
  "password": "your_password"
}
```

---

### Access Protected APIs

```http
GET /api/users
Authorization: Bearer <your_token>
```

---

## 📡 API Response Format

```json
{
  "code": 0,
  "message": "Success",
  "data": {}
}
```

### Error Example

```json
{
  "code": 404,
  "message": "User not found",
  "data": null
}
```

---

## 📚 API Endpoints

### Auth

* `POST /api/auth/login`

### Users

* `POST /api/users`
* `GET /api/users/{id}`
* `GET /api/users`
* `PUT /api/users/{id}`
* `DELETE /api/users/{id}`
* `PUT /api/users/{id}/roles`

### Roles

* `POST /api/roles`
* `GET /api/roles/{id}`
* `GET /api/roles`
* `PUT /api/roles/{id}`
* `DELETE /api/roles/{id}`
* `PUT /api/roles/{id}/permissions`

---

## ⚠️ Notes

* Only `POST /api/auth/login` is publicly accessible
* All other `/api/**` endpoints require a **JWT token**
* No refresh token mechanism (simplified design)
* Spring Security is not used (custom implementation)

---

## 🛡️ Best Practices

* Store secrets in environment variables
* Never commit:

  * database passwords
  * JWT secrets
* Use `.gitignore` for local config files
* Rotate credentials regularly in real environments

---

## ✅ Summary

* Secure by default (no hardcoded credentials)
* Flexible configuration via environment variables
* Clean RBAC implementation
* Suitable for learning and extension

