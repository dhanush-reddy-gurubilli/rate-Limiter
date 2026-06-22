package com.systemdesign.ratelimiter.algorithm.leakybucket;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service that implements the leaky bucket rate-limiting algorithm per client.
 */
@Slf4j
@Service
public class LeakyBucketRateLimiterService {

    /**
     * In-memory bucket state keyed by client identifier.
     */
    private final ConcurrentMap<String, BucketState> buckets = new ConcurrentHashMap<>();

    /**
     * Maximum amount of water that each client bucket can hold.
     */
    private final int capacity;

    /**
     * Amount of water drained from each bucket per second.
     */
    private final double leakRatePerSecond;

    /**
     * Clock used for elapsed-time calculations.
     */
    private final Clock clock;

    /**
     * Creates the service using leaky bucket values from application configuration.
     *
     * @param capacity maximum bucket capacity configured by application property
     * @param leakRatePerSecond configured amount of water drained every second
     */
    @Autowired
    public LeakyBucketRateLimiterService(
            @Value("${rate-limiter.leaky-bucket.capacity:10}") int capacity,
            @Value("${rate-limiter.leaky-bucket.leak-rate-per-second:1}") double leakRatePerSecond) {
        this(capacity, leakRatePerSecond, Clock.systemUTC());
    }

    /**
     * Creates the service with an explicit clock, mainly for deterministic tests.
     *
     * @param capacity maximum bucket capacity
     * @param leakRatePerSecond amount of water drained every second
     * @param clock clock used to calculate elapsed leak time
     */
    LeakyBucketRateLimiterService(int capacity, double leakRatePerSecond, Clock clock) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be greater than zero");
        }
        if (leakRatePerSecond <= 0) {
            throw new IllegalArgumentException("leakRatePerSecond must be greater than zero");
        }
        this.capacity = capacity;
        this.leakRatePerSecond = leakRatePerSecond;
        this.clock = clock;
    }

    /**
     * Applies the leaky bucket algorithm for a client request.
     *
     * @param clientId client identifier whose bucket should be checked
     * @return accepted decision when the bucket has capacity, otherwise rejected decision
     */
    public LeakyBucketDecision allowRequest(String clientId) {
        log.debug("Entered into allowRequest at {}", clock.millis());
        BucketState bucket = buckets.computeIfAbsent(clientId, ignored -> new BucketState(clock.millis()));
        synchronized (bucket) {
            // Synchronize per bucket so requests for the same client update water level atomically.
            leak(bucket);
            log.debug("Current water level is {}",bucket.currentWaterLevel());
            if (bucket.currentWaterLevel() + 1 > capacity) {
                return LeakyBucketDecision.rejected(clientId, bucket.currentWaterLevel(), capacity, leakRatePerSecond);
            }

            bucket.addRequest();
            log.debug("Current water level after adding request {}",bucket.currentWaterLevel());
            return LeakyBucketDecision.allowed(clientId, bucket.currentWaterLevel(), capacity, leakRatePerSecond);
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
     * Returns the active leaky bucket configuration.
     *
     * @return immutable configuration response
     */
    public LeakyBucketConfiguration configuration() {
        return new LeakyBucketConfiguration(capacity, leakRatePerSecond);
    }

    /**
     * Drains the bucket according to the elapsed time since the last update.
     *
     * @param bucket client bucket to update
     */
    private void leak(BucketState bucket) {
        log.debug("Entered into leak method at {}",clock.millis());
        long nowMillis = clock.millis();
        long elapsedMillis = Math.max(0, nowMillis - bucket.lastUpdatedMillis());
        // Convert elapsed milliseconds into the amount of water that should leak out.
        double leakedRequests = (elapsedMillis / 1000.0) * leakRatePerSecond;
        log.debug("nowMillis={}, lastUpdatedMillis={}  Elapsed Millis={} , leakedRequests={}",
                nowMillis,bucket.lastUpdatedMillis(),elapsedMillis, leakedRequests);
        if (leakedRequests > 0) {
            bucket.leak(leakedRequests, nowMillis);
        }
    }
}
