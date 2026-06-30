package com.systemdesign.ratelimiter.controller;

import com.systemdesign.ratelimiter.algorithm.slidingwindowlog.SlidingWindowLogConfiguration;
import com.systemdesign.ratelimiter.algorithm.slidingwindowlog.SlidingWindowLogDecision;
import com.systemdesign.ratelimiter.algorithm.slidingwindowlog.SlidingWindowLogRateLimiterService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes HTTP endpoints for the sliding window log rate limiter.
 */
@Validated
@RestController
@RequestMapping("/api/v1/rate-limit/sliding-window-log")
public class SlidingWindowLogRateLimiterController {

    /**
     * Service that owns the sliding window log algorithm and in-memory request log state.
     */
    private final SlidingWindowLogRateLimiterService slidingWindowLogRateLimiterService;

    /**
     * Creates the controller with the sliding window log rate limiter service dependency.
     *
     * @param slidingWindowLogRateLimiterService service used to check, reset, and read limiter configuration
     */
    public SlidingWindowLogRateLimiterController(
            SlidingWindowLogRateLimiterService slidingWindowLogRateLimiterService) {
        this.slidingWindowLogRateLimiterService = slidingWindowLogRateLimiterService;
    }

    /**
     * Checks whether a request from the given client is allowed by the sliding window log limiter.
     *
     * @param clientId unique client identifier used to track that client's request log
     * @return HTTP 200 with the decision when allowed, or HTTP 429 when the window limit is reached
     */
    @PostMapping("/check")
    public ResponseEntity<SlidingWindowLogDecision> check(@RequestParam @NotBlank String clientId) {
        // Ask the service to apply the sliding window log algorithm for this client.
        SlidingWindowLogDecision decision = slidingWindowLogRateLimiterService.allowRequest(clientId);

        // Map the algorithm decision to the correct HTTP status code.
        HttpStatus status = decision.allowed() ? HttpStatus.OK : HttpStatus.TOO_MANY_REQUESTS;
        return ResponseEntity.status(status).body(decision);
    }

    /**
     * Clears the stored sliding window log state for one client.
     *
     * @param clientId unique client identifier whose request log should be removed
     * @return HTTP 204 when the client request log has been reset
     */
    @DeleteMapping("/clients")
    public ResponseEntity<Void> resetClient(@RequestParam @NotBlank String clientId) {
        slidingWindowLogRateLimiterService.resetClient(clientId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns the active sliding window log configuration used by this application instance.
     *
     * @return configured max requests and window size
     */
    @GetMapping("/configuration")
    public SlidingWindowLogConfiguration configuration() {
        return slidingWindowLogRateLimiterService.configuration();
    }
}
