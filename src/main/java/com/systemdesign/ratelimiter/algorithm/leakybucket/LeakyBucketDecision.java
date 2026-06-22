package com.systemdesign.ratelimiter.algorithm.leakybucket;

/**
 * Immutable response that describes the result of a leaky bucket rate-limit check.
 *
 * @param clientId client identifier used for the rate-limit check
 * @param allowed whether the request was accepted by the limiter
 * @param currentWaterLevel bucket water level after applying leakage and the request decision
 * @param capacity maximum configured bucket capacity
 * @param leakRatePerSecond configured amount of water drained every second
 * @param message human-readable explanation of the decision
 */
public record LeakyBucketDecision(
        String clientId,
        boolean allowed,
        double currentWaterLevel,
        int capacity,
        double leakRatePerSecond,
        String message) {

    /**
     * Builds a decision for a request that was accepted by the leaky bucket limiter.
     *
     * @param clientId client identifier used for the rate-limit check
     * @param currentWaterLevel bucket water level after accepting the request
     * @param capacity maximum configured bucket capacity
     * @param leakRatePerSecond configured amount of water drained every second
     * @return accepted leaky bucket decision
     */
    static LeakyBucketDecision allowed(
            String clientId,
            double currentWaterLevel,
            int capacity,
            double leakRatePerSecond) {
        return new LeakyBucketDecision(
                clientId,
                true,
                currentWaterLevel,
                capacity,
                leakRatePerSecond,
                "Request accepted by leaky bucket limiter");
    }

    /**
     * Builds a decision for a request that was rejected because the bucket is full.
     *
     * @param clientId client identifier used for the rate-limit check
     * @param currentWaterLevel bucket water level after applying leakage
     * @param capacity maximum configured bucket capacity
     * @param leakRatePerSecond configured amount of water drained every second
     * @return rejected leaky bucket decision
     */
    static LeakyBucketDecision rejected(
            String clientId,
            double currentWaterLevel,
            int capacity,
            double leakRatePerSecond) {
        return new LeakyBucketDecision(
                clientId,
                false,
                currentWaterLevel,
                capacity,
                leakRatePerSecond,
                "Request rejected because the bucket is full");
    }
}
