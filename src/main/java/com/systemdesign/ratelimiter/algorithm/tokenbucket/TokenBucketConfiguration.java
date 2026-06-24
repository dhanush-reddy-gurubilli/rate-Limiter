package com.systemdesign.ratelimiter.algorithm.tokenbucket;

/**
 * Immutable view of the active token bucket limiter configuration.
 *
 * @param capacity maximum number of tokens the bucket can hold
 * @param refillRatePerSecond amount of tokens added to the bucket every second
 */
public record TokenBucketConfiguration(int capacity, double refillRatePerSecond) {
}
