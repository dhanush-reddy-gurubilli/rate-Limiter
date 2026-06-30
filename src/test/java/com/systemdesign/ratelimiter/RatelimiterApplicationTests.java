package com.systemdesign.ratelimiter;

import static org.assertj.core.api.Assertions.assertThat;

import com.systemdesign.ratelimiter.algorithm.fixedwindow.FixedWindowConfiguration;
import com.systemdesign.ratelimiter.algorithm.fixedwindow.FixedWindowDecision;
import com.systemdesign.ratelimiter.algorithm.fixedwindow.FixedWindowRateLimiterService;
import com.systemdesign.ratelimiter.algorithm.leakybucket.LeakyBucketConfiguration;
import com.systemdesign.ratelimiter.algorithm.leakybucket.LeakyBucketDecision;
import com.systemdesign.ratelimiter.algorithm.leakybucket.LeakyBucketRateLimiterService;
import com.systemdesign.ratelimiter.algorithm.tokenbucket.TokenBucketConfiguration;
import com.systemdesign.ratelimiter.algorithm.tokenbucket.TokenBucketDecision;
import com.systemdesign.ratelimiter.algorithm.tokenbucket.TokenBucketRateLimiterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"rate-limiter.leaky-bucket.capacity=2",
		"rate-limiter.leaky-bucket.leak-rate-per-second=0.000001",
		"rate-limiter.token-bucket.capacity=2",
		"rate-limiter.token-bucket.refill-rate-per-second=0.000001",
		"rate-limiter.fixed-window.max-requests=2",
		"rate-limiter.fixed-window.window-size-seconds=60"
})
class RatelimiterApplicationTests {

	@Autowired
	private LeakyBucketRateLimiterService leakyBucketRateLimiterService;

	@Autowired
	private TokenBucketRateLimiterService tokenBucketRateLimiterService;

	@Autowired
	private FixedWindowRateLimiterService fixedWindowRateLimiterService;

	@Test
	void contextLoads() {
	}

	@Test
	void leakyBucketRejectsRequestWhenBucketCapacityIsReached() {
		String clientId = "leaky-bucket-test-client";
		leakyBucketRateLimiterService.resetClient(clientId);
		LeakyBucketConfiguration configuration = leakyBucketRateLimiterService.configuration();

		for (int request = 0; request < configuration.capacity(); request++) {
			LeakyBucketDecision decision = leakyBucketRateLimiterService.allowRequest(clientId);

			assertThat(decision.allowed()).isTrue();
			assertThat(decision.clientId()).isEqualTo(clientId);
		}

		LeakyBucketDecision rejectedDecision = leakyBucketRateLimiterService.allowRequest(clientId);

		assertThat(rejectedDecision.allowed()).isFalse();
		assertThat(rejectedDecision.capacity()).isEqualTo(configuration.capacity());
		assertThat(rejectedDecision.currentWaterLevel())
				.isBetween(configuration.capacity() - 0.001, (double) configuration.capacity());
	}

	@Test
	void tokenBucketRejectsRequestWhenTokensAreConsumed() {
		String clientId = "token-bucket-test-client";
		tokenBucketRateLimiterService.resetClient(clientId);
		TokenBucketConfiguration configuration = tokenBucketRateLimiterService.configuration();

		for (int request = 0; request < configuration.capacity(); request++) {
			TokenBucketDecision decision = tokenBucketRateLimiterService.allowRequest(clientId);

			assertThat(decision.allowed()).isTrue();
			assertThat(decision.clientId()).isEqualTo(clientId);
		}

		TokenBucketDecision rejectedDecision = tokenBucketRateLimiterService.allowRequest(clientId);

		assertThat(rejectedDecision.allowed()).isFalse();
		assertThat(rejectedDecision.capacity()).isEqualTo(configuration.capacity());
		assertThat(rejectedDecision.currentTokens()).isBetween(0.0, 0.001);
	}

	@Test
	void fixedWindowRejectsRequestWhenWindowLimitIsReached() {
		String clientId = "fixed-window-test-client";
		fixedWindowRateLimiterService.resetClient(clientId);
		FixedWindowConfiguration configuration = fixedWindowRateLimiterService.configuration();

		for (int request = 0; request < configuration.maxRequests(); request++) {
			FixedWindowDecision decision = fixedWindowRateLimiterService.allowRequest(clientId);

			assertThat(decision.allowed()).isTrue();
			assertThat(decision.clientId()).isEqualTo(clientId);
		}

		FixedWindowDecision rejectedDecision = fixedWindowRateLimiterService.allowRequest(clientId);

		assertThat(rejectedDecision.allowed()).isFalse();
		assertThat(rejectedDecision.maxRequests()).isEqualTo(configuration.maxRequests());
		assertThat(rejectedDecision.windowSizeSeconds()).isEqualTo(configuration.windowSizeSeconds());
		assertThat(rejectedDecision.requestCount()).isEqualTo(configuration.maxRequests());
	}

}
