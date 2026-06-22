package com.systemdesign.ratelimiter.controller;

import com.systemdesign.ratelimiter.algorithm.leakybucket.LeakyBucketConfiguration;
import com.systemdesign.ratelimiter.algorithm.leakybucket.LeakyBucketDecision;
import com.systemdesign.ratelimiter.algorithm.leakybucket.LeakyBucketRateLimiterService;
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
 * REST controller that exposes HTTP endpoints for the leaky bucket rate limiter.
 */
@Validated
@RestController
@RequestMapping("/api/v1/rate-limit/leaky-bucket")
public class LeakyBucketRateLimiterController {

    /**
     * Service that owns the leaky bucket algorithm and in-memory bucket state.
     */
    private final LeakyBucketRateLimiterService leakyBucketRateLimiterService;

    /**
     * Creates the controller with the leaky bucket rate limiter service dependency.
     *
     * @param leakyBucketRateLimiterService service used to check, reset, and read limiter configuration
     */
    public LeakyBucketRateLimiterController(LeakyBucketRateLimiterService leakyBucketRateLimiterService) {
        this.leakyBucketRateLimiterService = leakyBucketRateLimiterService;
    }

    /**
     * Checks whether a request from the given client is allowed by the leaky bucket limiter.
     *
     * @param clientId unique client identifier used to track that client's bucket
     * @return HTTP 200 with the decision when allowed, or HTTP 429 when the bucket is full
     */
    @PostMapping("/check")
    public ResponseEntity<LeakyBucketDecision> check(@RequestParam @NotBlank String clientId) {
        // Ask the service to apply the leaky bucket algorithm for this client.
        LeakyBucketDecision decision = leakyBucketRateLimiterService.allowRequest(clientId);

        // Map the algorithm decision to the correct HTTP status code.
        HttpStatus status = decision.allowed() ? HttpStatus.OK : HttpStatus.TOO_MANY_REQUESTS;
        return ResponseEntity.status(status).body(decision);
    }

    /**
     * Clears the stored leaky bucket state for one client.
     *
     * @param clientId unique client identifier whose bucket should be removed
     * @return HTTP 204 when the client bucket has been reset
     */
    @DeleteMapping("/clients")
    public ResponseEntity<Void> resetClient(@RequestParam @NotBlank String clientId) {
        leakyBucketRateLimiterService.resetClient(clientId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns the active leaky bucket configuration used by this application instance.
     *
     * @return configured bucket capacity and leak rate
     */
    @GetMapping("/configuration")
    public LeakyBucketConfiguration configuration() {
        return leakyBucketRateLimiterService.configuration();
    }
}
