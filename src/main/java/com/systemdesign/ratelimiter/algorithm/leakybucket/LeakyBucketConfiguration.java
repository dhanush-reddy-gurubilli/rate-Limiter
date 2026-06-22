package com.systemdesign.ratelimiter.algorithm.leakybucket;

/**
 * Immutable view of the active leaky bucket limiter configuration.
 *
 * @param capacity maximum water level the bucket can hold before rejecting requests
 * @param leakRatePerSecond amount of water drained from the bucket every second
 */
public record LeakyBucketConfiguration(int capacity, double leakRatePerSecond) {
}
