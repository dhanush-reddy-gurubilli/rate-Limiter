package com.systemdesign.ratelimiter.algorithm.fixedwindow;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service that implements the fixed window rate-limiting algorithm per client.
 */
@Slf4j
@Service
public class FixedWindowRateLimiterService {

    /**
     * In-memory fixed window state keyed by client identifier.
     */
    private final ConcurrentMap<String, FixedWindowState> windows = new ConcurrentHashMap<>();

    /**
     * Maximum number of requests allowed in each fixed window.
     */
    private final int maxRequests;

    /**
     * Fixed window duration in milliseconds.
     */
    private final long windowSizeMillis;

    /**
     * Clock used for window calculations.
     */
    private final Clock clock;

    /**
     * Creates the service using fixed window values from application configuration.
     *
     * @param maxRequests maximum requests configured by application property
     * @param windowSizeSeconds configured fixed window duration in seconds
     */
    @Autowired
    public FixedWindowRateLimiterService(
            @Value("${rate-limiter.fixed-window.max-requests:10}") int maxRequests,
            @Value("${rate-limiter.fixed-window.window-size-seconds:60}") long windowSizeSeconds) {
        this(maxRequests, windowSizeSeconds, Clock.systemUTC());
    }

    /**
     * Creates the service with an explicit clock, mainly for deterministic tests.
     *
     * @param maxRequests maximum accepted requests per window
     * @param windowSizeSeconds fixed window duration in seconds
     * @param clock clock used to calculate active windows
     */
    FixedWindowRateLimiterService(int maxRequests, long windowSizeSeconds, Clock clock) {
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
     * Applies the fixed window algorithm for a client request.
     *
     * @param clientId client identifier whose window should be checked
     * @return accepted decision when the window has capacity, otherwise rejected decision
     */
    public FixedWindowDecision allowRequest(String clientId) {
        log.debug("Entered into fixed window allowRequest at {}", clock.millis());
        long currentWindowStartMillis = currentWindowStartMillis();
        FixedWindowState window = windows.computeIfAbsent(
                clientId,
                ignored -> new FixedWindowState(currentWindowStartMillis));

        synchronized (window) {
            // Synchronize per client window so request count updates are atomic.
            if (window.windowStartMillis() != currentWindowStartMillis) {
                window.resetWindow(currentWindowStartMillis);
            }

            log.debug("Current request count is {}", window.requestCount());
            if (window.requestCount() >= maxRequests) {
                return FixedWindowDecision.rejected(
                        clientId,
                        window.requestCount(),
                        maxRequests,
                        windowSizeSeconds(),
                        window.windowStartMillis());
            }

            window.incrementRequestCount();
            log.debug("Current request count after accepting request {}", window.requestCount());
            return FixedWindowDecision.allowed(
                    clientId,
                    window.requestCount(),
                    maxRequests,
                    windowSizeSeconds(),
                    window.windowStartMillis());
        }
    }

    /**
     * Removes the stored window for the given client.
     *
     * @param clientId client identifier whose fixed window state should be cleared
     */
    public void resetClient(String clientId) {
        windows.remove(clientId);
    }

    /**
     * Returns the active fixed window configuration.
     *
     * @return immutable configuration response
     */
    public FixedWindowConfiguration configuration() {
        return new FixedWindowConfiguration(maxRequests, windowSizeSeconds());
    }

    /**
     * Calculates the aligned start timestamp for the current fixed window.
     *
     * @return active fixed window start timestamp in milliseconds
     */
    private long currentWindowStartMillis() {
        long nowMillis = clock.millis();
        return (nowMillis / windowSizeMillis) * windowSizeMillis;
    }

    private long windowSizeSeconds() {
        return windowSizeMillis / 1_000;
    }
}
