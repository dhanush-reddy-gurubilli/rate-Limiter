# Java Concepts Used In This Project

This document explains the Java language keywords and important concepts used in this rate limiter project.

## Package

`package` groups related Java classes under a namespace.

Example:

```java
package com.systemdesign.ratelimiter.algorithm.leakybucket;
```

This means the class belongs to the `com.systemdesign.ratelimiter.algorithm.leakybucket` package. Packages help avoid name conflicts and keep code organized.

## Import

`import` lets a class use another class without writing its full package name every time.

Example:

```java
import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
```

Without imports, the code would need to use names like `java.time.Clock` everywhere.

## Class

`class` defines a blueprint for objects. A class can contain fields, constructors, and methods.

Example:

```java
public class LeakyBucketRateLimiterService {
}
```

In this project, service and controller classes are normal Java classes managed by Spring.

## Record

`record` is a compact Java type for immutable data carriers.

Example:

```java
public record LeakyBucketConfiguration(int capacity, double leakRatePerSecond) {
}
```

Java automatically creates:

- A constructor
- Getter-like accessor methods, such as `capacity()`
- `equals`
- `hashCode`
- `toString`

Records are used here for response/configuration objects because they hold data and do not need mutable behavior.

## Public

`public` means the class, constructor, or method can be accessed from other packages.

Example:

```java
public LeakyBucketDecision allowRequest(String clientId)
```

The controller can call this service method because it is public.

## Private

`private` means the field or method can only be accessed inside the same class.

Example:

```java
private final int capacity;
```

This protects internal service state from direct outside modification.

## Package-Private

If no access modifier is written, Java uses package-private access.

Example:

```java
final class BucketState {
}
```

`BucketState` can be used by classes in the same package, but not by classes outside `algorithm.leakybucket`.

## Static

`static` means the member belongs to the class itself, not to one object instance.

Example:

```java
public static void main(String[] args)
```

The JVM starts the application by calling this static `main` method.

Another example:

```java
static LeakyBucketDecision allowed(...)
```

This is a static factory method. It creates a `LeakyBucketDecision` without requiring an existing decision object.

## Final

`final` means something cannot be reassigned, overridden, or extended depending on where it is used.

Field example:

```java
private final Clock clock;
```

The `clock` reference must be assigned once in the constructor and cannot point to another `Clock` later.

Class example:

```java
final class BucketState {
}
```

No other class can extend `BucketState`.

## Synchronized

`synchronized` allows only one thread at a time to execute a block for the same lock object.

Example:

```java
synchronized (bucket) {
    leak(bucket);
    bucket.addRequest();
}
```

This matters because many HTTP requests can hit the same client bucket at the same time. Synchronizing on the client bucket makes the water-level update atomic for that client.

## Constructor

A constructor creates and initializes an object.

Example:

```java
LeakyBucketRateLimiterService(int capacity, double leakRatePerSecond, Clock clock) {
    this.capacity = capacity;
    this.leakRatePerSecond = leakRatePerSecond;
    this.clock = clock;
}
```

The service constructor validates configuration and stores required dependencies.

## This

`this` refers to the current object.

Example:

```java
this.capacity = capacity;
```

The left side is the field on the object. The right side is the constructor parameter.

## New

`new` creates an object.

Example:

```java
new ConcurrentHashMap<>()
```

This creates the in-memory map used to store client bucket states.

## Return

`return` exits a method and optionally gives a value back to the caller.

Example:

```java
return LeakyBucketDecision.allowed(clientId, bucket.currentWaterLevel(), capacity, leakRatePerSecond);
```

This returns the final rate-limit decision to the controller.

## If

`if` runs code only when a condition is true.

Example:

```java
if (bucket.currentWaterLevel() + 1 > capacity) {
    return LeakyBucketDecision.rejected(...);
}
```

This checks whether adding another request would overflow the bucket.

## Throw

`throw` raises an exception.

Example:

```java
throw new IllegalArgumentException("capacity must be greater than zero");
```

This stops invalid configuration from creating a broken rate limiter.

## Extends

`extends` means one class inherits from another class.

Example from tests:

```java
private static final class MutableClock extends Clock {
}
```

`MutableClock` extends Java's `Clock` class so tests can control time manually.

## Override

`@Override` means a method replaces a method declared in a parent class or interface.

Example:

```java
@Override
public Instant instant() {
    return instant;
}
```

This tells Java that `MutableClock` is providing its own implementation of `Clock.instant()`.

## Primitive Types

Primitive types store simple values directly.

Examples used in this project:

```java
int capacity;
double leakRatePerSecond;
long nowMillis;
boolean allowed;
```

`int` stores whole numbers, `double` stores decimal numbers, `long` stores larger whole numbers, and `boolean` stores `true` or `false`.

## String

`String` stores text.

Example:

```java
String clientId
```

The rate limiter uses `clientId` to identify which bucket belongs to which client.

## Generic Types

Generics let classes work with specific types while preserving compile-time safety.

Example:

```java
ConcurrentMap<String, BucketState>
```

This means the map keys are `String` values and the map values are `BucketState` objects.

## Diamond Operator

`<>` lets Java infer generic types from the variable declaration.

Example:

```java
new ConcurrentHashMap<>()
```

Java understands this means `new ConcurrentHashMap<String, BucketState>()`.

## Lambda Expression

A lambda is a compact function expression.

Example:

```java
buckets.computeIfAbsent(clientId, ignored -> new BucketState(clock.millis()));
```

If a bucket does not exist for the client, the lambda creates a new `BucketState`.

## Method Call

A method call runs behavior on an object or class.

Example:

```java
bucket.currentWaterLevel()
```

This calls the `currentWaterLevel` method on the `bucket` object.

## Encapsulation

Encapsulation means hiding internal state and exposing controlled methods.

Example:

```java
private double currentWaterLevel;

void addRequest() {
    currentWaterLevel++;
}
```

Code outside `BucketState` cannot directly change `currentWaterLevel`; it must use methods.

## Immutability

Immutable data cannot be changed after creation.

Records like `LeakyBucketDecision` and `LeakyBucketConfiguration` are immutable. This is useful for API responses because each response represents one completed decision.

## Mutability

Mutable objects can change after creation.

`BucketState` is mutable because the water level changes whenever requests arrive or time passes.

## Thread Safety

Thread safety means code behaves correctly when multiple threads use it at the same time.

This project uses:

- `ConcurrentHashMap` for safe concurrent access to the bucket map.
- `synchronized (bucket)` for safe updates to one client's bucket.

## ConcurrentHashMap

`ConcurrentHashMap` is a thread-safe map implementation.

Example:

```java
private final ConcurrentMap<String, BucketState> buckets = new ConcurrentHashMap<>();
```

It allows multiple clients' buckets to be looked up safely while the application handles concurrent HTTP requests.

## Interface

An interface defines behavior without tying code to one implementation.

Example:

```java
ConcurrentMap<String, BucketState>
```

`ConcurrentMap` is the interface. `ConcurrentHashMap` is the actual implementation.

## Clock

`Clock` is a Java time abstraction.

Production code uses:

```java
Clock.systemUTC()
```

Tests use a custom mutable clock so time can be advanced without waiting in real life.

## Math.max

`Math.max(a, b)` returns the larger value.

Example:

```java
currentWaterLevel = Math.max(0, currentWaterLevel - leakedRequests);
```

This prevents the bucket water level from going below zero.

## JavaDoc

JavaDoc comments document classes, fields, constructors, methods, and record parameters.

Example:

```java
/**
 * Applies the leaky bucket algorithm for a client request.
 */
```

These comments are readable in IDEs and can be used to generate documentation.
