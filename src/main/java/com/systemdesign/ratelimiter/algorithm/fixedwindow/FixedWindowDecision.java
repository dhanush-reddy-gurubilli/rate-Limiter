package com.systemdesign.ratelimiter.algorithm.fixedwindow;

/**
 * Immutable response that describes the result of a fixed window rate-limit check.
 *
 * @param clientId client identifier used for the rate-limit check
 * @param allowed whether the request was accepted by the limiter
 * @param requestCount accepted request count in the active window after the decision
 * @param maxRequests maximum accepted requests per window
 * @param windowSizeSeconds fixed window duration in seconds
 * @param windowStartMillis start timestamp of the active window
 * @param message human-readable explanation of the decision
 */
public record FixedWindowDecision(
        String clientId,
        boolean allowed,
        int requestCount,
        int maxRequests,
        long windowSizeSeconds,
        long windowStartMillis,
        String message) {

    /**
     * Builds a decision for a request that was accepted by the fixed window limiter.
     *
     * @param clientId client identifier used for the rate-limit check
     * @param requestCount accepted request count after accepting the request
     * @param maxRequests maximum accepted requests per window
     * @param windowSizeSeconds fixed window duration in seconds
     * @param windowStartMillis start timestamp of the active window
     * @return accepted fixed window decision
     */
    static FixedWindowDecision allowed(
            String clientId,
            int requestCount,
            int maxRequests,
            long windowSizeSeconds,
            long windowStartMillis) {
        return new FixedWindowDecision(
                clientId,
                true,
                requestCount,
                maxRequests,
                windowSizeSeconds,
                windowStartMillis,
                "Request accepted by fixed window limiter");
    }

    /**
     * Builds a decision for a request that was rejected because the window limit was reached.
     *
     * @param clientId client identifier used for the rate-limit check
     * @param requestCount accepted request count in the active window
     * @param maxRequests maximum accepted requests per window
     * @param windowSizeSeconds fixed window duration in seconds
     * @param windowStartMillis start timestamp of the active window
     * @return rejected fixed window decision
     */
    static FixedWindowDecision rejected(
            String clientId,
            int requestCount,
            int maxRequests,
            long windowSizeSeconds,
            long windowStartMillis) {
        return new FixedWindowDecision(
                clientId,
                false,
                requestCount,
                maxRequests,
                windowSizeSeconds,
                windowStartMillis,
                "Request rejected because the fixed window limit is reached");
    }
}
