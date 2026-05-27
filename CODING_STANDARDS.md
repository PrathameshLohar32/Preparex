# Spring Boot Backend Coding Standards for AI Generated Code

# Objective

This document defines the mandatory coding standards, architecture principles, and implementation guidelines for all backend services built using Spring Boot.

The AI must strictly follow these standards while generating code.

---

# Tech Stack

- Java 21+
- Spring Boot
- Spring Security
- PostgreSQL
- Redis
- Maven
- JPA/Hibernate
- Flyway
- JWT Authentication
- Lombok
- MapStruct (preferred for mappings)

---

# Core Principles

- Follow clean architecture principles
- Keep code modular and scalable
- Prefer composition over inheritance
- Use SOLID principles
- Write production-grade code
- Avoid code duplication
- Keep methods small and readable
- Avoid unnecessary abstractions
- Prefer constructor injection over field injection
- Avoid tight coupling
- Maintain separation of concerns

---

# Mandatory API Response Structure

ALL APIs MUST return responses in the following format:

```json
{
  "success": true,
  "message": "User details fetched successfully",
  "data": {}
}
```

Error response:

```json
{
  "success": false,
  "message": "Invalid credentials",
  "data": null
}
```

---

# Standard API Response Wrapper

Always create and use:

```java
public class ApiResponse<T> {

    private boolean success;

    private String message;

    private T data;
}
```

Rules:
- Never return raw entities directly
- Never return plain strings
- Never return ResponseEntity<Object>
- Always wrap responses using ApiResponse<T>

---

# Exception Handling Standards

## Mandatory Rules

- ALWAYS use Global Exception Handler
- NEVER throw generic exceptions like:
  - RuntimeException
  - Exception
  - NullPointerException
  - IllegalArgumentException

- ALWAYS create custom exceptions

---

# Global Exception Handler

Must use:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
}
```

Handle:
- Validation exceptions
- Authentication exceptions
- Authorization exceptions
- Resource not found exceptions
- Business exceptions
- Internal server exceptions

---

# Custom Exceptions

Examples:

```java
public class UserNotFoundException extends BaseException {
}

public class InvalidOtpException extends BaseException {
}

public class SessionExpiredException extends BaseException {
}

public class DuplicateResourceException extends BaseException {
}
```

---

# Base Exception Structure

```java
public abstract class BaseException extends RuntimeException {

    private final String errorCode;

    protected BaseException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
```

---

# Validation Standards

- Use Bean Validation annotations
- NEVER manually validate request fields unnecessarily

Example:

```java
@NotBlank
@Email
private String email;
```

Use:

```java
@Valid
@RequestBody
```

---

# Logging Standards

Use structured logging.

Mandatory:
- Log request start/end
- Log external API calls
- Log failures
- Log important business events

NEVER:
- Log passwords
- Log OTPs
- Log access tokens
- Log refresh tokens

Use:

```java
@Slf4j
```

Example:

```java
log.info("User login successful for userId={}", userId);
```

---

# Package Structure

```text
com.prepnex.auth
│
├── config
├── controller
├── dto
│   ├── request
│   ├── response
│
├── entity
├── enums
├── exception
├── filter
├── mapper
├── repository
├── security
├── service
│   ├── impl
│   ├── strategy
│   ├── factory
│
├── util
├── validator
└── constant
```

---

# Layer Responsibilities

## Controller
- Only request/response handling
- No business logic

## Service
- Business logic only

## Repository
- Database operations only

## DTO
- Request/response models only

## Entity
- Persistence models only

---

# DTO Rules

- NEVER expose entities directly in APIs
- Always use DTOs
- Separate request and response DTOs

Example:

```java
LoginRequestDto
UserResponseDto
```

---

# Entity Standards

Rules:
- Use UUID as primary keys
- Add auditing fields
- Use soft delete if needed

Mandatory fields:

```java
createdAt
updatedAt
createdBy
updatedBy
```

Use:

```java
@Entity
@Table(name = "users")
```

Additional rule:

- Extend `BaseEntity` for all JPA entities to provide `createdAt` and `updatedAt` auditing fields. Use the `com.prepnex.prepnex_backend.commons.BaseEntity` superclass rather than redefining these fields in each entity.

Example `BaseEntity` implementation to include in the codebase:

```java
package com.prepnex.prepnex_backend.commons;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public class BaseEntity {

  @CreationTimestamp
  @Column(name = "created_at", columnDefinition = "TIMESTAMP", updatable = false)
  private Date createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
  private Date updatedAt;

}
```

---

# JPA Standards

- Prefer LAZY loading
- Avoid EAGER loading
- Use pagination for list APIs
- Avoid N+1 query problems
- Use projections when needed

---

# Repository Standards

- Extend JpaRepository
- Avoid writing native queries unnecessarily
- Use Specifications for dynamic filters

---

# Service Standards

- Interfaces are mandatory for services
- Implementation classes should be inside impl package

Example:

```java
UserService
UserServiceImpl
```

---

# Security Standards

## Authentication

- Use JWT Access Token
- Use Refresh Tokens
- Store refresh token hashes only
- Use Redis for active sessions

## Passwords

Use:
- Argon2 preferred
- BCrypt acceptable

NEVER:
- Store plain passwords
- Log passwords

---

# Session Management Standards

- Max 2 active sessions per user
- Store active sessions in Redis
- Use TTL for sessions
- Rotate refresh tokens

---

# Redis Standards

Use Redis only for:
- Active sessions
- OTPs
- Rate limiting
- Temporary registration data

Do NOT use Redis for:
- Long-term history
- Permanent storage

---

# Controller Standards

Rules:
- Use RESTful naming
- Use proper HTTP methods
- Use versioned APIs

Example:

```text
/api/v1/auth/login
```

---

# API Naming Standards

Good:

```text
POST /api/v1/auth/login
GET /api/v1/users/{id}
```

Bad:

```text
/getUser
/doLogin
```

---

# HTTP Status Standards

Use proper status codes:

- 200 OK
- 201 CREATED
- 400 BAD REQUEST
- 401 UNAUTHORIZED
- 403 FORBIDDEN
- 404 NOT FOUND
- 409 CONFLICT
- 500 INTERNAL SERVER ERROR

---

# Configuration Standards

- Use application.yml
- Separate configs by environment

Example:

```text
application-dev.yml
application-prod.yml
```

- Never hardcode secrets
- Use environment variables

---

# Constants Standards

- Avoid magic strings
- Use constants class

Example:

```java
public final class SecurityConstants {
}
```

---

# Mapper Standards

Use MapStruct for DTO mappings.

Do NOT manually map large objects repeatedly.

---

# Transaction Standards

Use:

```java
@Transactional
```

Rules:
- Apply only where needed
- Avoid long transactions

---

# Async Processing

Use async processing for:
- Emails
- Notifications
- Analytics
- Logging pipelines

---

# Rate Limiting

Mandatory for:
- Login APIs
- OTP APIs
- Refresh token APIs

Preferred:
- Redis-based rate limiting

---

# OTP Standards

- OTP expiry: 5 minutes
- OTP retry limit mandatory
- OTP resend cooldown mandatory

---

# Swagger Standards

Use OpenAPI/Swagger documentation.

Every API must contain:
- Summary
- Description
- Request examples
- Response examples

---

# Testing Standards

Mandatory:
- Unit tests
- Service layer tests
- Integration tests

Preferred:
- Testcontainers

Use:
- JUnit 5
- Mockito

---

# Code Quality Standards

Mandatory:
- Meaningful variable names
- Meaningful method names
- No commented dead code
- No unused imports
- No duplicate logic
- No huge classes
- No methods longer than ~40 lines if avoidable

---

# Naming Standards

## Classes
- PascalCase

## Methods
- camelCase

## Constants
- UPPER_SNAKE_CASE

## Packages
- lowercase

---

# Performance Standards

- Use pagination
- Avoid loading unnecessary data
- Use indexes properly
- Cache only when required

---

# Database Standards

- Use Flyway migrations
- Never use ddl-auto=create in production
- Add indexes for frequently queried columns

---

# Flyway Standards

Migration naming:

```text
V1__create_users_table.sql
V2__create_sessions_table.sql
```

---

# Documentation Standards

Every service method should contain:
- Purpose
- Important business notes
- Edge cases if necessary

---

# Industrial Best Practices

Mandatory:
- Correlation IDs for tracing
- Structured logging
- Centralized exception handling
- Environment-based configs
- API versioning
- DTO-based APIs
- Proper validation
- Redis TTLs
- Refresh token rotation
- Secure password hashing
- Rate limiting
- Clean architecture

---

# AI Instructions

While generating code:

- Follow layered architecture strictly
- Do not generate shortcut implementations
- Do not skip validation
- Do not use field injection
- Do not expose entities directly
- Do not throw generic exceptions
- Always generate production-grade code
- Always keep extensibility in mind
- Prefer readable code over clever code
