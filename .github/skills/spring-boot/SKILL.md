---
name: spring-boot
description: "Expert workflow for building and reviewing Java Maven Spring Boot 3.x applications. Use when creating Spring Boot REST APIs, services, JPA persistence, security, tests, configuration, Docker deployment, or production-ready Java code."
argument-hint: "Describe the Spring Boot feature, service, API, or project to build"
user-invocable: true
---

# Spring Boot Maven Development

Build maintainable, secure, testable Java 21+ applications with Spring Boot 3.x and Maven. Apply only the layers and dependencies needed for the requested behavior.

## Before Coding

1. Inspect the Maven wrapper, `pom.xml`, source layout, existing tests, configuration, and local conventions.
2. Identify the smallest owning layer: web controller, application service, persistence adapter, configuration, or security.
3. State a falsifiable behavior hypothesis and select the cheapest relevant test or build command before editing.
4. Preserve public API contracts and avoid unrelated refactors. When requirements are unclear, ask a focused question before inventing business rules.

## Project Baseline

- Use Java 21 or later, Spring Boot 3.x, Jakarta APIs, and Maven Wrapper (`./mvnw`) when present.
- Keep `pom.xml` dependency versions managed by Spring Boot's BOM unless a justified override is required.
- Organize packages by feature when practical; otherwise use clear controller, service, repository, domain, dto, configuration, and exception boundaries.
- Prefer records for immutable request, response, and value DTOs. Use classes for JPA entities and components requiring framework mutation or proxying.
- Use constructor injection exclusively. Do not use field injection; omit `@Autowired` for a single constructor.
- Declare constructor-injected dependency fields `private final`; use `@Component`, `@Service`, `@Repository`, and `@Controller` or `@RestController` according to each component's responsibility.
- Use descriptive PascalCase type names, camelCase members, and `UPPER_SNAKE_CASE` constants.
- Program to interfaces when a meaningful boundary exists. Do not add interfaces solely for one trivial implementation.
- Make stateless utility classes `final` with a private constructor; prefer focused domain services over general-purpose utility classes.
- Add comments only for non-obvious decisions, constraints, or tradeoffs. Do not narrate self-explanatory code or dependency usage.

## Web APIs

1. Define request and response DTOs; never expose JPA entities directly from controllers.
2. Use `@RestController`, precise request mappings, correct HTTP methods, and `ResponseEntity` when status or headers vary.
3. Validate inbound DTOs with Jakarta Validation annotations and `@Valid`.
4. Put business rules in services and persistence in repositories; controllers coordinate HTTP concerns only.
5. Return consistent error responses from a global `@RestControllerAdvice`, including a stable error code, human-readable message, HTTP status, and relevant validation fields.
6. Use pagination and explicit sorting for collection endpoints that can grow. Avoid leaking implementation details in URLs or error messages.

## Persistence

- Use Spring Data JPA repositories for ordinary aggregate persistence; use explicit queries only when they improve correctness or performance.
- Model relationships deliberately, default collections to lazy loading, and avoid accidental N+1 access through projections, fetch joins, entity graphs, or batch strategies as appropriate.
- Define database constraints and indexes that protect application invariants and expected query paths.
- Use migrations such as Flyway or Liquibase for schema evolution in non-trivial persistent applications; do not depend on automatic schema updates in production.
- Put transaction boundaries in service methods using `@Transactional`, with `readOnly = true` for read paths where appropriate.

## Configuration And Security

- Bind grouped configuration with validated `@ConfigurationProperties`; reserve `@Value` for isolated simple values.
- Use either `application.yml` or `application.properties` consistently with the existing project; prefer readable, structured configuration without converting formats unnecessarily.
- Use profiles for environment-specific non-secret settings. Source secrets from environment variables or an approved secret manager, never source control.
- Secure endpoints with Spring Security using least privilege and explicit authorization rules.
- Use a modern password encoder such as BCrypt or Argon2 when storing passwords. Never log credentials, tokens, or sensitive personal data.
- Configure CORS narrowly for browser clients; do not use wildcard origins with credentials.
- Treat all external input as untrusted and validate it before use. Use parameterized ORM/query APIs or `NamedParameterJdbcTemplate` rather than constructing query strings from input, and encode untrusted output in HTML contexts.

## Logging

- Use SLF4J through the framework's logging facade; declare a `private static final Logger` when direct logger access is needed.
- Use parameterized messages such as `logger.info("Processing order {}", orderId)` rather than string concatenation.
- Log actionable context at appropriate levels without exposing secrets, credentials, tokens, or sensitive personal data.

## Testing And Validation

1. Write focused JUnit 5 unit tests for service behavior and edge cases; use Mockito only at real boundaries.
2. Test controllers with `@WebMvcTest` and MockMvc when web behavior is the subject.
3. Test repositories with `@DataJpaTest`; use Testcontainers for database-dependent integration behavior.
4. Use `@SpringBootTest` only when the interaction under test requires the full application context.
5. Verify each substantive change with the narrowest meaningful Maven command, then run `./mvnw test` before completion when feasible.
6. Use `./mvnw package` for a release-oriented build check when applicable; it runs the Maven test phase unless explicitly configured otherwise. Use `./mvnw spring-boot:run` to run the application and `./mvnw spring-boot:build-image` for supported container-image builds.
7. Fix new failures caused by the change. Report pre-existing failures separately without masking them.


## Completion Checklist

- The code uses idiomatic Java and Spring Boot conventions with minimal, cohesive components.
- Input validation, error handling, and authorization match the endpoint's risk.
- Persistence behavior avoids avoidable lazy-loading and transaction pitfalls.
- Tests cover the changed behavior at the appropriate layer.
- Relevant Maven checks pass, configuration is externalized, and sensitive data is absent from code and logs.