package com.systemdesign.ratelimiter.algorithm.tokenbucket;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service that implements the token bucket rate-limiting algorithm per client.
 */
@Slf4j
@Service
public class TokenBucketRateLimiterService {

    /**
     * In-memory bucket state keyed by client identifier.
     */
    private final ConcurrentMap<String, TokenBucketState> buckets = new ConcurrentHashMap<>();

    /**
     * Maximum number of tokens that each client bucket can hold.
     */
    private final int capacity;

    /**
     * Amount of tokens added to each bucket per second.
     */
    private final double refillRatePerSecond;

    /**
     * Clock used for elapsed-time calculations.
     */
    private final Clock clock;

    /**
     * Creates the service using token bucket values from application configuration.
     *
     * @param capacity maximum token capacity configured by application property
     * @param refillRatePerSecond configured amount of tokens added every second
     */
    @Autowired
    public TokenBucketRateLimiterService(
            @Value("${rate-limiter.token-bucket.capacity:10}") int capacity,
            @Value("${rate-limiter.token-bucket.refill-rate-per-second:1}") double refillRatePerSecond) {
        this(capacity, refillRatePerSecond, Clock.systemUTC());
    }

    /**
     * Creates the service with an explicit clock, mainly for deterministic tests.
     *
     * @param capacity maximum bucket capacity
     * @param refillRatePerSecond amount of tokens added every second
     * @param clock clock used to calculate elapsed refill time
     */
    TokenBucketRateLimiterService(int capacity, double refillRatePerSecond, Clock clock) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be greater than zero");
        }
        if (refillRatePerSecond <= 0) {
            throw new IllegalArgumentException("refillRatePerSecond must be greater than zero");
        }
        this.capacity = capacity;
        this.refillRatePerSecond = refillRatePerSecond;
        this.clock = clock;
    }

    /**
     * Applies the token bucket algorithm for a client request.
     *
     * @param clientId client identifier whose bucket should be checked
     * @return accepted decision when a token is available, otherwise rejected decision
     */
    public TokenBucketDecision allowRequest(String clientId) {
        log.debug("Entered into token bucket allowRequest at {}", clock.millis());
        TokenBucketState bucket = buckets.computeIfAbsent(
                clientId,
                ignored -> new TokenBucketState(capacity, clock.millis()));
        synchronized (bucket) {
            // Synchronize per bucket so requests for the same client update tokens atomically.
            refill(bucket);
            log.debug("Current token count is {}", bucket.currentTokens());
            if (bucket.currentTokens() < 1) {
                return TokenBucketDecision.rejected(clientId, bucket.currentTokens(), capacity, refillRatePerSecond);
            }

            bucket.consumeToken();
            log.debug("Current token count after consuming token {}", bucket.currentTokens());
            return TokenBucketDecision.allowed(clientId, bucket.currentTokens(), capacity, refillRatePerSecond);
        }
    }

    /**
     * Removes the stored bucket for the given client.
     *
     * @param clientId client identifier whose bucket state should be cleared
     */
    public void resetClient(String clientId) {
        buckets.remove(clientId);
    }

    /**
     * Returns the active token bucket configuration.
     *
     * @return immutable configuration response
     */
    public TokenBucketConfiguration configuration() {
        return new TokenBucketConfiguration(capacity, refillRatePerSecond);
    }

    /**
     * Refills the bucket according to the elapsed time since the last update.
     *
     * @param bucket client bucket to update
     */
    private void refill(TokenBucketState bucket) {
        log.debug("Entered into refill method at {}", clock.millis());
        long nowMillis = clock.millis();
        long elapsedMillis = Math.max(0, nowMillis - bucket.lastRefilledMillis());
        // Convert elapsed milliseconds into the amount of tokens that should be added.
        double tokensToAdd = (elapsedMillis / 1000.0) * refillRatePerSecond;
        log.debug(
                "nowMillis={}, lastRefilledMillis={}  elapsedMillis={} , tokensToAdd={}",
                nowMillis,
                bucket.lastRefilledMillis(),
                elapsedMillis,
                tokensToAdd);
        if (tokensToAdd > 0) {
            bucket.refill(tokensToAdd, capacity, nowMillis);
        }
    }
}
