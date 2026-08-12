---
name: java-junit
description: "Expert workflow for writing and reviewing Java JUnit 5 unit tests, including Mockito isolation, assertions, test organization, and data-driven parameterized tests. Use when creating, fixing, or reviewing Java tests."
argument-hint: "Describe the Java behavior, class, or test suite to test"
user-invocable: true
---

# JUnit 5 Testing

Write focused, deterministic Java 21+ tests with JUnit Jupiter. Prefer the smallest test scope that proves the requested behavior and keep tests readable as executable specifications.

## Before Writing Tests

1. Inspect the class under test, its public contract, existing tests, test dependencies, and build configuration.
2. Identify observable behavior, meaningful edge cases, and real external boundaries that require isolation.
3. Use the existing test style and assertion library when it is clear and consistent; avoid changing the test framework for a narrow task.
4. Run the narrowest relevant Maven command after each substantive test change, then run the appropriate broader suite when feasible.

## Project Setup

- Put tests in `src/test/java` and mirror the production package structure where practical.
- For Maven projects, use JUnit Jupiter API, engine, and `junit-jupiter-params` when parameterized tests are needed. Let Spring Boot's dependency management control their versions in Spring Boot projects.
- Run tests with `./mvnw test` when the Maven Wrapper exists; otherwise use `mvn test`.
- Use Maven Failsafe for integration tests that need a separate lifecycle, naming them `*IT`; retain Maven Surefire and the `*Test` suffix for unit tests.

## Test Structure

- Name test classes with the `Test` suffix, such as `CalculatorTest`, and name test methods after behavior, for example `calculateTotal_shouldApplyDiscount_whenCustomerIsEligible`.
- Use Arrange-Act-Assert. Keep the act phase obvious and assertions close to the behavior they prove.
- Test one behavior per test. A test may use `assertAll` for multiple outcomes of that one behavior.
- Keep tests independent, idempotent, and safe to run in any order. Do not rely on shared mutable state or outcomes from other tests.
- Use `@DisplayName` when it makes reports clearer, especially for `@Nested` contexts or parameterized cases.
- Use `@BeforeEach` and `@AfterEach` only for repeated setup or cleanup. Use `@BeforeAll` and `@AfterAll` sparingly; they must be static unless the test class uses `@TestInstance(TestInstance.Lifecycle.PER_CLASS)`.
- Use `@Nested` test classes to group related scenarios without hiding setup or creating deep nesting.

## Assertions And Exceptions

- Use JUnit Jupiter assertions from `org.junit.jupiter.api.Assertions`, or the project-standard fluent library such as AssertJ when it improves clarity.
- Prefer specific assertions over broad boolean checks. Assert meaningful values, types, collections, and side effects.
- Test expected failures with `assertThrows`, then assert relevant properties of the exception. Use `assertDoesNotThrow` only when absence of an exception is itself the behavior under test.
- Use `assertAll` for related assertions so a single run reports all mismatches.
- Provide assertion messages only when the failure would otherwise be unclear; use lazy message suppliers for expensive diagnostics.

## Parameterized Tests

- Use `@ParameterizedTest` when the same behavior must hold for multiple inputs. Keep each data set meaningful and label cases with a readable test name pattern when helpful.
- Use `@ValueSource` for a single primitive or string argument, `@EnumSource` for enum constants, and `@CsvSource` for small inline tuples.
- Use `@MethodSource` for complex arguments, generated cases, or when named test data makes intent clearer. Keep source factories near their tests and return a `Stream`, collection, or other supported source.
- Use `@CsvFileSource` only for stable, readable classpath datasets. Do not use external mutable files or production data as test inputs.
- Include boundaries, null or blank cases where valid, and representative invalid inputs. Do not parameterize unrelated scenarios merely to reduce line count.

## Mocking And Isolation

- Prefer real value objects and small fakes over mocks when they make the test clearer. Mock only genuine external boundaries or expensive, nondeterministic collaborators.
- With Mockito, use `@ExtendWith(MockitoExtension.class)` for annotation-based mocks. Inject dependencies through the production constructor; avoid field injection in both production and tests.
- Verify observable results first. Verify collaborator interactions only when the interaction is part of the contract, and avoid overspecifying call order or implementation details.
- Do not mock JDK types, domain value objects, or the class under test. Resetting mocks within a test is usually a sign that the test covers too many behaviors.

## Organization And Test Control

- Organize tests by feature or component through packages and test classes. Use `@Tag` for intentional categories such as `fast`, `integration`, or `slow`, and configure build filtering explicitly.
- Avoid execution-order dependencies. Use `@TestMethodOrder` and `@Order` only when testing an explicitly ordered workflow that cannot be expressed as independent tests.
- Use `@Disabled` only temporarily, provide a reason and tracking reference when available, and do not use it to hide a regression.
- For concurrent, file, network, database, or clock-dependent code, explicitly control the dependency with a test double, temporary resource, Testcontainers, or injectable clock as appropriate.

## Completion Checklist

- Tests describe behavior and cover the changed happy path, boundaries, and failure behavior.
- Tests are deterministic, independent, and do not leak mutable state or external resources.
- Assertions check outcomes that matter rather than incidental implementation details.
- Parameterized tests use a source suited to the data and include meaningful cases.
- Relevant Maven tests pass, and any pre-existing failures are reported separately.