package com.systemdesign.ratelimiter.controller;

import com.systemdesign.ratelimiter.algorithm.fixedwindow.FixedWindowConfiguration;
import com.systemdesign.ratelimiter.algorithm.fixedwindow.FixedWindowDecision;
import com.systemdesign.ratelimiter.algorithm.fixedwindow.FixedWindowRateLimiterService;
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
 * REST controller that exposes HTTP endpoints for the fixed window rate limiter.
 */
@Validated
@RestController
@RequestMapping("/api/v1/rate-limit/fixed-window")
public class FixedWindowRateLimiterController {

    /**
     * Service that owns the fixed window algorithm and in-memory window state.
     */
    private final FixedWindowRateLimiterService fixedWindowRateLimiterService;

    /**
     * Creates the controller with the fixed window rate limiter service dependency.
     *
     * @param fixedWindowRateLimiterService service used to check, reset, and read limiter configuration
     */
    public FixedWindowRateLimiterController(FixedWindowRateLimiterService fixedWindowRateLimiterService) {
        this.fixedWindowRateLimiterService = fixedWindowRateLimiterService;
    }

    /**
     * Checks whether a request from the given client is allowed by the fixed window limiter.
     *
     * @param clientId unique client identifier used to track that client's window
     * @return HTTP 200 with the decision when allowed, or HTTP 429 when the window limit is reached
     */
    @PostMapping("/check")
    public ResponseEntity<FixedWindowDecision> check(@RequestParam @NotBlank String clientId) {
        // Ask the service to apply the fixed window algorithm for this client.
        FixedWindowDecision decision = fixedWindowRateLimiterService.allowRequest(clientId);

        // Map the algorithm decision to the correct HTTP status code.
        HttpStatus status = decision.allowed() ? HttpStatus.OK : HttpStatus.TOO_MANY_REQUESTS;
        return ResponseEntity.status(status).body(decision);
    }

    /**
     * Clears the stored fixed window state for one client.
     *
     * @param clientId unique client identifier whose window should be removed
     * @return HTTP 204 when the client window has been reset
     */
    @DeleteMapping("/clients")
    public ResponseEntity<Void> resetClient(@RequestParam @NotBlank String clientId) {
        fixedWindowRateLimiterService.resetClient(clientId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns the active fixed window configuration used by this application instance.
     *
     * @return configured max requests and window size
     */
    @GetMapping("/configuration")
    public FixedWindowConfiguration configuration() {
        return fixedWindowRateLimiterService.configuration();
    }
}
