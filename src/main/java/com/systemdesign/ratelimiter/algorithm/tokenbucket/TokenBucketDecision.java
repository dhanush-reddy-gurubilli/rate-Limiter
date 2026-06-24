package com.systemdesign.ratelimiter.algorithm.tokenbucket;

/**
 * Immutable response that describes the result of a token bucket rate-limit check.
 *
 * @param clientId client identifier used for the rate-limit check
 * @param allowed whether the request was accepted by the limiter
 * @param currentTokens bucket token count after applying refill and the request decision
 * @param capacity maximum configured bucket capacity
 * @param refillRatePerSecond configured amount of tokens added every second
 * @param message human-readable explanation of the decision
 */
public record TokenBucketDecision(
        String clientId,
        boolean allowed,
        double currentTokens,
        int capacity,
        double refillRatePerSecond,
        String message) {

    /**
     * Builds a decision for a request that was accepted by the token bucket limiter.
     *
     * @param clientId client identifier used for the rate-limit check
     * @param currentTokens bucket token count after accepting the request
     * @param capacity maximum configured bucket capacity
     * @param refillRatePerSecond configured amount of tokens added every second
     * @return accepted token bucket decision
     */
    static TokenBucketDecision allowed(
            String clientId,
            double currentTokens,
            int capacity,
            double refillRatePerSecond) {
        return new TokenBucketDecision(
                clientId,
                true,
                currentTokens,
                capacity,
                refillRatePerSecond,
                "Request accepted by token bucket limiter");
    }

    /**
     * Builds a decision for a request that was rejected because no token is available.
     *
     * @param clientId client identifier used for the rate-limit check
     * @param currentTokens bucket token count after applying refill
     * @param capacity maximum configured bucket capacity
     * @param refillRatePerSecond configured amount of tokens added every second
     * @return rejected token bucket decision
     */
    static TokenBucketDecision rejected(
            String clientId,
            double currentTokens,
            int capacity,
            double refillRatePerSecond) {
        return new TokenBucketDecision(
                clientId,
                false,
                currentTokens,
                capacity,
                refillRatePerSecond,
                "Request rejected because no token is available");
    }
}
