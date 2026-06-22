# Spring Boot Concepts Used In This Project

This document explains the Spring Boot, web, validation, logging, testing, and design-pattern concepts used in this rate limiter project.

## Spring Boot

Spring Boot is a framework that starts a Spring application with sensible defaults. It reduces manual configuration by auto-configuring web servers, JSON handling, validation, logging, and application context setup.

In this project, Spring Boot starts the API server and wires the controller and service together.

## Application Entry Point

The application starts from:

```java
public static void main(String[] args) {
    SpringApplication.run(RatelimiterApplication.class, args);
}
```

`SpringApplication.run` starts the Spring container, scans for components, applies auto-configuration, and starts the embedded web server.

## @SpringBootApplication

`@SpringBootApplication` marks the main application class.

It combines three important Spring annotations:

- `@Configuration`: allows the class to define Spring configuration.
- `@EnableAutoConfiguration`: tells Spring Boot to configure libraries found on the classpath.
- `@ComponentScan`: scans the current package and subpackages for Spring components.

Because `RatelimiterApplication` is in `com.systemdesign.ratelimiter`, Spring scans controllers and services under that package.

## Spring Container

The Spring container is the runtime system that creates, stores, configures, and injects application objects.

These managed objects are called beans.

In this project, examples of beans include:

- `LeakyBucketRateLimiterController`
- `LeakyBucketRateLimiterService`

## Spring Bean

A Spring bean is an object managed by the Spring container.

Spring creates the bean, injects its dependencies, and controls its lifecycle. By default, Spring beans are singleton scoped, meaning one shared instance is used for the application.

## Singleton Scope

The default Spring bean scope is singleton.

For `LeakyBucketRateLimiterService`, this means the in-memory `buckets` map is shared across requests handled by this application instance.

That is why thread safety matters in the service.

## @Service

`@Service` marks a class as a service-layer Spring bean.

Example:

```java
@Service
public class LeakyBucketRateLimiterService {
}
```

Spring detects this class during component scanning and creates a bean for it.

## @RestController

`@RestController` marks a class as a REST API controller.

It combines:

- `@Controller`: marks the class as a Spring MVC controller.
- `@ResponseBody`: writes return values directly to the HTTP response body.

Because of `@RestController`, returned records like `LeakyBucketDecision` are serialized to JSON automatically.

## @RequestMapping

`@RequestMapping` defines a common URL path for all endpoints in a controller.

Example:

```java
@RequestMapping("/api/v1/rate-limit/leaky-bucket")
```

Every method endpoint inside the controller starts with this base path.

## @PostMapping

`@PostMapping` maps an HTTP `POST` request to a Java method.

Example:

```java
@PostMapping("/check")
```

The full path becomes:

```text
POST /api/v1/rate-limit/leaky-bucket/check
```

This endpoint checks whether a request is allowed.

## @DeleteMapping

`@DeleteMapping` maps an HTTP `DELETE` request to a Java method.

In this project, it resets one client's bucket:

```text
DELETE /api/v1/rate-limit/leaky-bucket/clients?clientId=user-1
```

## @GetMapping

`@GetMapping` maps an HTTP `GET` request to a Java method.

In this project, it reads the current leaky bucket configuration:

```text
GET /api/v1/rate-limit/leaky-bucket/configuration
```

## @RequestParam

`@RequestParam` binds a query parameter from the URL to a Java method parameter.

Example:

```java
public ResponseEntity<LeakyBucketDecision> check(@RequestParam String clientId)
```

For this URL:

```text
/check?clientId=12345
```

Spring passes `"12345"` into the `clientId` parameter.

## @Validated

`@Validated` enables validation for controller method parameters.

In this project, it allows annotations like `@NotBlank` on request parameters to be checked.

## @NotBlank

`@NotBlank` is a Jakarta Validation annotation.

Example:

```java
@RequestParam @NotBlank String clientId
```

It rejects null, empty, or whitespace-only `clientId` values.

## @Autowired

`@Autowired` tells Spring to inject dependencies.

Example:

```java
@Autowired
public LeakyBucketRateLimiterService(...)
```

Spring calls this constructor and supplies the configured values.

For controller dependencies, this project uses constructor injection without explicitly writing `@Autowired`, which Spring supports when there is only one constructor.

## Constructor Injection

Constructor injection means dependencies are passed through the constructor.

Example:

```java
public LeakyBucketRateLimiterController(LeakyBucketRateLimiterService service) {
    this.leakyBucketRateLimiterService = service;
}
```

This is preferred because required dependencies are clear and can be marked `final`.

## @Value

`@Value` injects values from application properties.

Example:

```java
@Value("${rate-limiter.leaky-bucket.capacity:10}") int capacity
```

This reads `rate-limiter.leaky-bucket.capacity` from `application.properties`. If the property is missing, it uses the default value `10`.

## application.properties

`application.properties` stores application configuration.

Current project values:

```properties
spring.application.name=ratelimiter
rate-limiter.leaky-bucket.capacity=10
rate-limiter.leaky-bucket.leak-rate-per-second=1
logging.level.root=INFO
logging.level.com.systemdesign.ratelimiter=DEBUG
```

Spring Boot automatically loads this file from `src/main/resources`.

## ResponseEntity

`ResponseEntity` lets a controller control the HTTP response status and body.

Example:

```java
return ResponseEntity.status(status).body(decision);
```

This project returns:

- HTTP `200 OK` when the request is allowed.
- HTTP `429 Too Many Requests` when the bucket is full.
- HTTP `204 No Content` when a client bucket is reset.

## HttpStatus

`HttpStatus` is an enum of HTTP status codes.

Example:

```java
HttpStatus.OK
HttpStatus.TOO_MANY_REQUESTS
```

The controller maps the rate-limit decision to the right HTTP status.

## JSON Serialization

Spring Boot WebMVC includes JSON serialization support.

When the controller returns a record like `LeakyBucketDecision`, Spring converts it to JSON:

```json
{
  "clientId": "12345",
  "allowed": true,
  "currentWaterLevel": 1.0,
  "capacity": 10,
  "leakRatePerSecond": 1.0,
  "message": "Request accepted by leaky bucket limiter"
}
```

This is handled behind the hood by Spring MVC message converters.

## Spring MVC Request Flow

For a request like:

```text
POST /api/v1/rate-limit/leaky-bucket/check?clientId=12345
```

The flow is:

1. Embedded server receives the HTTP request.
2. Spring MVC's dispatcher receives the request.
3. Spring matches the path to `LeakyBucketRateLimiterController.check`.
4. `@RequestParam` extracts `clientId`.
5. Validation checks `@NotBlank`.
6. Controller calls `LeakyBucketRateLimiterService.allowRequest`.
7. Service returns a `LeakyBucketDecision`.
8. Controller wraps it in `ResponseEntity`.
9. Spring serializes the response object to JSON.
10. The HTTP response is sent back to the client.

## Dispatcher Servlet

The Dispatcher Servlet is the central Spring MVC component that routes incoming HTTP requests to controller methods.

You do not create it manually. Spring Boot auto-configures it because the WebMVC starter is present.

## Auto-Configuration

Auto-configuration is Spring Boot's mechanism for configuring common application pieces based on dependencies.

Because this project includes WebMVC, validation, actuator, and logging dependencies, Spring Boot configures the related infrastructure automatically.

## Component Scanning

Component scanning finds Spring-managed classes such as controllers and services.

Because the main class is in `com.systemdesign.ratelimiter`, Spring scans subpackages like:

```text
com.systemdesign.ratelimiter.controller
com.systemdesign.ratelimiter.algorithm.leakybucket
```

## Dependency Injection

Dependency injection means an object receives the objects or values it needs from the container instead of constructing them itself.

Example:

```java
LeakyBucketRateLimiterController -> LeakyBucketRateLimiterService
```

The controller does not create the service with `new`; Spring supplies it.

## Inversion Of Control

Inversion of Control means the framework controls object creation and wiring.

Instead of the application manually creating controllers and services, Spring creates them, connects them, and calls them when HTTP requests arrive.

## Design Patterns Used

This project uses several common design patterns:

| Pattern | Where used | Purpose |
| --- | --- | --- |
| MVC | Controller + service separation | Keeps HTTP handling separate from business logic |
| Dependency Injection | Controller receives service, service receives config | Makes dependencies explicit and testable |
| Singleton Bean | Spring service bean | Shares limiter state inside one app instance |
| DTO | `LeakyBucketDecision`, `LeakyBucketConfiguration` | Transfers structured API data |
| Static Factory Method | `LeakyBucketDecision.allowed/rejected` | Creates readable response objects |
| Strategy-like Clock Injection | Test constructor receives `Clock` | Lets tests control time behavior |

## Lombok

Lombok is a compile-time code generation library.

This project uses:

```java
@Slf4j
```

Lombok generates a logger field named `log` during compilation.

## @Slf4j

`@Slf4j` adds an SLF4J logger to the class.

It allows calls like:

```java
log.debug("Current water level is {}", bucket.currentWaterLevel());
```

The `{}` placeholders are filled by the logging framework only when the log level is enabled.

## Logging Levels

Logging levels control which messages are printed.

Common levels:

- `ERROR`: serious failures
- `WARN`: unexpected but recoverable situations
- `INFO`: normal high-level application events
- `DEBUG`: detailed information for development and troubleshooting
- `TRACE`: very detailed diagnostic information

This project configures:

```properties
logging.level.root=INFO
logging.level.com.systemdesign.ratelimiter=DEBUG
```

That means most libraries log at `INFO`, but this project's package logs at `DEBUG`.

## Spring Boot Starters

Spring Boot starters are dependency bundles.

This project uses:

| Starter | Purpose |
| --- | --- |
| `spring-boot-starter-webmvc` | Builds REST APIs with Spring MVC |
| `spring-boot-starter-validation` | Enables Jakarta Validation annotations |
| `spring-boot-starter-actuator` | Adds operational endpoints such as health checks |
| `spring-boot-starter-*-test` | Adds test support for the corresponding starter |

## Actuator

Spring Boot Actuator provides production-friendly endpoints for observing an application.

This project includes the actuator starter, so endpoints such as health information can be exposed under `/actuator`.

## @SpringBootTest

`@SpringBootTest` starts the Spring application context during tests.

Example:

```java
@SpringBootTest
class RatelimiterApplicationTests {
}
```

The `contextLoads` test verifies that Spring can start the application and create all required beans.

## @Test

`@Test` marks a method as a JUnit test.

Example:

```java
@Test
void allowsRequestsUntilBucketCapacityIsReached() {
}
```

Maven runs these tests during `mvn test`.

## AssertJ

AssertJ is a fluent assertion library used in tests.

Example:

```java
assertThat(service.allowRequest("client-1").allowed()).isTrue();
```

This checks that the rate limiter allowed the request.

## Behind The Hood: Bean Creation

At startup, Spring roughly does this:

1. Reads `RatelimiterApplication`.
2. Scans packages for annotations like `@RestController` and `@Service`.
3. Creates `LeakyBucketRateLimiterService`.
4. Reads `@Value` properties for capacity and leak rate.
5. Creates `LeakyBucketRateLimiterController`.
6. Injects the service into the controller constructor.
7. Registers controller methods as HTTP routes.

## Behind The Hood: A Rate Limit Request

When `/check` is called:

1. Spring MVC resolves the controller method.
2. It converts the query parameter into a `String`.
3. Validation checks the parameter.
4. The controller calls the service.
5. The service gets or creates a bucket from `ConcurrentHashMap`.
6. The service synchronizes on that bucket.
7. It leaks water based on elapsed time.
8. It accepts or rejects the request.
9. Spring converts the returned record into JSON.

## Important Production Note

The current bucket storage is in-memory.

This is good for learning and single-instance demos, but in production with multiple app instances, each instance would have its own bucket map. A distributed system should store bucket state in Redis or another shared store.
