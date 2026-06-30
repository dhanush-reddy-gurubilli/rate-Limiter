package com.systemdesign.ratelimiter.algorithm.slidingwindowlog;

/**
 * Immutable view of the active sliding window log limiter configuration.
 *
 * @param maxRequests maximum accepted requests inside the sliding window
 * @param windowSizeSeconds sliding window duration in seconds
 */
public record SlidingWindowLogConfiguration(int maxRequests, long windowSizeSeconds) {
}
