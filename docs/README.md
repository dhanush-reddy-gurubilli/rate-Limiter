# Rate Limiting Algorithms

This folder explains each rate-limiting algorithm in a simple, interview-friendly way.

## Algorithms

| Algorithm | Best for | Notes |
| --- | --- | --- |
| Leaky Bucket | Smoothing traffic to a steady processing rate | Handles bursts by queueing until the bucket is full |
| Token Bucket | Allowing controlled bursts | Usually better for public APIs because it supports burst capacity |
| Fixed Window Counter | Simple request counting | Easy to implement, but boundary bursts are a problem |
| Sliding Window Log | Accurate limiting | More memory-heavy because request timestamps are stored |
| Sliding Window Counter | Balanced accuracy and memory | Common production-friendly alternative |

## Learning Path

1. Start with an in-memory single-node implementation.
2. Add tests that prove the algorithm behavior.
3. Move state to Redis so multiple app instances share the same limits.
4. Add user/API-key based limits.
5. Add observability: rejected request count, allowed request count, and latency.

## Suggested Project Structure

```text
controller/
  LeakyBucketRateLimiterController.java
  TokenBucketRateLimiterController.java
  FixedWindowRateLimiterController.java
algorithm/
  leakybucket/
    LeakyBucketRateLimiterService.java
    BucketState.java
    LeakyBucketDecision.java
    LeakyBucketConfiguration.java
  tokenbucket/
    TokenBucketRateLimiterService.java
    TokenBucketState.java
    TokenBucketDecision.java
    TokenBucketConfiguration.java
  fixedwindow/
    FixedWindowRateLimiterService.java
    FixedWindowState.java
    FixedWindowDecision.java
    FixedWindowConfiguration.java
docs/
  README.md
  leaky-bucket.md
  token-bucket.md
  fixed-window.md
```

For MAANG-style system design learning, keep the code simple first, then discuss production concerns separately: distributed state, clock behavior, race conditions, Redis Lua scripts, hot-key handling, and fallback behavior when Redis is down.
