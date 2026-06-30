package com.systemdesign.ratelimiter.algorithm.fixedwindow;

/**
 * Immutable view of the active fixed window limiter configuration.
 *
 * @param maxRequests maximum accepted requests per window
 * @param windowSizeSeconds fixed window duration in seconds
 */
public record FixedWindowConfiguration(int maxRequests, long windowSizeSeconds) {
}
