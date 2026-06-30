package com.systemdesign.ratelimiter.algorithm.slidingwindowlog;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service that implements the sliding window log rate-limiting algorithm per client.
 */
@Slf4j
@Service
public class SlidingWindowLogRateLimiterService {

    /**
     * In-memory sliding window log state keyed by client identifier.
     */
    private final ConcurrentMap<String, SlidingWindowLogState> windows = new ConcurrentHashMap<>();

    /**
     * Maximum number of requests allowed inside each sliding window.
     */
    private final int maxRequests;

    /**
     * Sliding window duration in milliseconds.
     */
    private final long windowSizeMillis;

    /**
     * Clock used for window calculations.
     */
    private final Clock clock;

    /**
     * Creates the service using sliding window log values from application configuration.
     *
     * @param maxRequests maximum requests configured by application property
     * @param windowSizeSeconds configured sliding window duration in seconds
     */
    @Autowired
    public SlidingWindowLogRateLimiterService(
            @Value("${rate-limiter.sliding-window-log.max-requests:10}") int maxRequests,
            @Value("${rate-limiter.sliding-window-log.window-size-seconds:60}") long windowSizeSeconds) {
        this(maxRequests, windowSizeSeconds, Clock.systemUTC());
    }

    /**
     * Creates the service with an explicit clock, mainly for deterministic tests.
     *
     * @param maxRequests maximum accepted requests per sliding window
     * @param windowSizeSeconds sliding window duration in seconds
     * @param clock clock used to calculate active windows
     */
    SlidingWindowLogRateLimiterService(int maxRequests, long windowSizeSeconds, Clock clock) {
        if (maxRequests <= 0) {
            throw new IllegalArgumentException("maxRequests must be greater than zero");
        }
        if (windowSizeSeconds <= 0) {
            throw new IllegalArgumentException("windowSizeSeconds must be greater than zero");
        }
        this.maxRequests = maxRequests;
        this.windowSizeMillis = windowSizeSeconds * 1_000;
        this.clock = clock;
    }

    /**
     * Applies the sliding window log algorithm for a client request.
     *
     * @param clientId client identifier whose request log should be checked
     * @return accepted decision when the sliding window has capacity, otherwise rejected decision
     */
    public SlidingWindowLogDecision allowRequest(String clientId) {
        log.debug("Entered into sliding window log allowRequest at {}", clock.millis());
        SlidingWindowLogState window = windows.computeIfAbsent(clientId, ignored -> new SlidingWindowLogState());

        synchronized (window) {
            // Synchronize per client window so pruning and request count updates are atomic.
            long nowMillis = clock.millis();
            long cutoffMillis = nowMillis - windowSizeMillis;
            window.removeExpiredRequests(cutoffMillis);

            log.debug("Current sliding window request count is {}", window.requestCount());
            if (window.requestCount() >= maxRequests) {
                return SlidingWindowLogDecision.rejected(
                        clientId,
                        window.requestCount(),
                        maxRequests,
                        windowSizeSeconds(),
                        window.oldestRequestMillis());
            }

            window.addRequest(nowMillis);
            log.debug("Current sliding window request count after accepting request {}", window.requestCount());
            return SlidingWindowLogDecision.allowed(
                    clientId,
                    window.requestCount(),
                    maxRequests,
                    windowSizeSeconds(),
                    window.oldestRequestMillis());
        }
    }

    /**
     * Removes the stored sliding window log for the given client.
     *
     * @param clientId client identifier whose sliding window log state should be cleared
     */
    public void resetClient(String clientId) {
        windows.remove(clientId);
    }

    /**
     * Returns the active sliding window log configuration.
     *
     * @return immutable configuration response
     */
    public SlidingWindowLogConfiguration configuration() {
        return new SlidingWindowLogConfiguration(maxRequests, windowSizeSeconds());
    }

    private long windowSizeSeconds() {
        return windowSizeMillis / 1_000;
    }
}
