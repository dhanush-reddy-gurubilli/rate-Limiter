package com.systemdesign.ratelimiter.algorithm.slidingwindowlog;

/**
 * Immutable response that describes the result of a sliding window log rate-limit check.
 *
 * @param clientId client identifier used for the rate-limit check
 * @param allowed whether the request was accepted by the limiter
 * @param requestCount accepted request count in the active sliding window after the decision
 * @param maxRequests maximum accepted requests inside the sliding window
 * @param windowSizeSeconds sliding window duration in seconds
 * @param oldestRequestMillis oldest retained request timestamp for the client, or zero when none exist
 * @param message human-readable explanation of the decision
 */
public record SlidingWindowLogDecision(
        String clientId,
        boolean allowed,
        int requestCount,
        int maxRequests,
        long windowSizeSeconds,
        long oldestRequestMillis,
        String message) {

    /**
     * Builds a decision for a request that was accepted by the sliding window log limiter.
     *
     * @param clientId client identifier used for the rate-limit check
     * @param requestCount accepted request count after accepting the request
     * @param maxRequests maximum accepted requests inside the sliding window
     * @param windowSizeSeconds sliding window duration in seconds
     * @param oldestRequestMillis oldest retained request timestamp for the client
     * @return accepted sliding window log decision
     */
    static SlidingWindowLogDecision allowed(
            String clientId,
            int requestCount,
            int maxRequests,
            long windowSizeSeconds,
            long oldestRequestMillis) {
        return new SlidingWindowLogDecision(
                clientId,
                true,
                requestCount,
                maxRequests,
                windowSizeSeconds,
                oldestRequestMillis,
                "Request accepted by sliding window log limiter");
    }

    /**
     * Builds a decision for a request that was rejected because the sliding window limit was reached.
     *
     * @param clientId client identifier used for the rate-limit check
     * @param requestCount accepted request count in the active sliding window
     * @param maxRequests maximum accepted requests inside the sliding window
     * @param windowSizeSeconds sliding window duration in seconds
     * @param oldestRequestMillis oldest retained request timestamp for the client
     * @return rejected sliding window log decision
     */
    static SlidingWindowLogDecision rejected(
            String clientId,
            int requestCount,
            int maxRequests,
            long windowSizeSeconds,
            long oldestRequestMillis) {
        return new SlidingWindowLogDecision(
                clientId,
                false,
                requestCount,
                maxRequests,
                windowSizeSeconds,
                oldestRequestMillis,
                "Request rejected because the sliding window log limit is reached");
    }
}
