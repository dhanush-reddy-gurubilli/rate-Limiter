package com.systemdesign.ratelimiter.controller;

import com.systemdesign.ratelimiter.algorithm.tokenbucket.TokenBucketConfiguration;
import com.systemdesign.ratelimiter.algorithm.tokenbucket.TokenBucketDecision;
import com.systemdesign.ratelimiter.algorithm.tokenbucket.TokenBucketRateLimiterService;
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
 * REST controller that exposes HTTP endpoints for the token bucket rate limiter.
 */
@Validated
@RestController
@RequestMapping("/api/v1/rate-limit/token-bucket")
public class TokenBucketRateLimiterController {

    /**
     * Service that owns the token bucket algorithm and in-memory bucket state.
     */
    private final TokenBucketRateLimiterService tokenBucketRateLimiterService;

    /**
     * Creates the controller with the token bucket rate limiter service dependency.
     *
     * @param tokenBucketRateLimiterService service used to check, reset, and read limiter configuration
     */
    public TokenBucketRateLimiterController(TokenBucketRateLimiterService tokenBucketRateLimiterService) {
        this.tokenBucketRateLimiterService = tokenBucketRateLimiterService;
    }

    /**
     * Checks whether a request from the given client is allowed by the token bucket limiter.
     *
     * @param clientId unique client identifier used to track that client's bucket
     * @return HTTP 200 with the decision when allowed, or HTTP 429 when no token is available
     */
    @PostMapping("/check")
    public ResponseEntity<TokenBucketDecision> check(@RequestParam @NotBlank String clientId) {
        // Ask the service to apply the token bucket algorithm for this client.
        TokenBucketDecision decision = tokenBucketRateLimiterService.allowRequest(clientId);

        // Map the algorithm decision to the correct HTTP status code.
        HttpStatus status = decision.allowed() ? HttpStatus.OK : HttpStatus.TOO_MANY_REQUESTS;
        return ResponseEntity.status(status).body(decision);
    }

    /**
     * Clears the stored token bucket state for one client.
     *
     * @param clientId unique client identifier whose bucket should be removed
     * @return HTTP 204 when the client bucket has been reset
     */
    @DeleteMapping("/clients")
    public ResponseEntity<Void> resetClient(@RequestParam @NotBlank String clientId) {
        tokenBucketRateLimiterService.resetClient(clientId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns the active token bucket configuration used by this application instance.
     *
     * @return configured bucket capacity and refill rate
     */
    @GetMapping("/configuration")
    public TokenBucketConfiguration configuration() {
        return tokenBucketRateLimiterService.configuration();
    }
}
