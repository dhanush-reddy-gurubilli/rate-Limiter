package com.systemdesign.ratelimiter.algorithm.tokenbucket;

/**
 * Mutable state for one client's token bucket.
 */
final class TokenBucketState {

    /**
     * Current number of available tokens in the bucket.
     */
    private double currentTokens;

    /**
     * Last time the bucket was refilled.
     */
    private long lastRefilledMillis;

    /**
     * Creates a token bucket state initialized at the supplied timestamp.
     *
     * @param currentTokens initial number of available tokens
     * @param lastRefilledMillis timestamp used as the initial refill reference point
     */
    TokenBucketState(double currentTokens, long lastRefilledMillis) {
        this.currentTokens = currentTokens;
        this.lastRefilledMillis = lastRefilledMillis;
    }

    /**
     * Returns the current available token count.
     *
     * @return current available token count
     */
    double currentTokens() {
        return currentTokens;
    }

    /**
     * Returns the timestamp of the last refill calculation.
     *
     * @return last refill timestamp in milliseconds
     */
    long lastRefilledMillis() {
        return lastRefilledMillis;
    }

    /**
     * Adds tokens up to the configured bucket capacity and moves the update timestamp forward.
     *
     * @param tokensToAdd number of tokens to add
     * @param capacity maximum number of tokens the bucket can hold
     * @param nowMillis timestamp used for this update
     */
    void refill(double tokensToAdd, int capacity, long nowMillis) {
        currentTokens = Math.min(capacity, currentTokens + tokensToAdd);
        lastRefilledMillis = nowMillis;
    }

    /**
     * Consumes one token for an accepted request.
     */
    void consumeToken() {
        currentTokens--;
    }
}
