package com.systemdesign.ratelimiter.algorithm.tokenbucket;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

class TokenBucketRateLimiterServiceTests {

    @Test
    void allowsRequestsUntilTokensAreConsumed() {
        MutableClock clock = new MutableClock();
        TokenBucketRateLimiterService service = new TokenBucketRateLimiterService(2, 1, clock);

        assertThat(service.allowRequest("client-1").allowed()).isTrue();
        assertThat(service.allowRequest("client-1").allowed()).isTrue();
        assertThat(service.allowRequest("client-1").allowed()).isFalse();
    }

    @Test
    void refillsTokensOverTime() {
        MutableClock clock = new MutableClock();
        TokenBucketRateLimiterService service = new TokenBucketRateLimiterService(2, 1, clock);

        service.allowRequest("client-1");
        service.allowRequest("client-1");

        clock.advanceMillis(1_000);

        TokenBucketDecision decision = service.allowRequest("client-1");

        assertThat(decision.allowed()).isTrue();
        assertThat(decision.currentTokens()).isZero();
    }

    @Test
    void capsRefilledTokensAtBucketCapacity() {
        MutableClock clock = new MutableClock();
        TokenBucketRateLimiterService service = new TokenBucketRateLimiterService(2, 1, clock);

        service.allowRequest("client-1");

        clock.advanceMillis(10_000);

        TokenBucketDecision decision = service.allowRequest("client-1");

        assertThat(decision.allowed()).isTrue();
        assertThat(decision.currentTokens()).isEqualTo(1);
    }

    @Test
    void keepsSeparateBucketsPerClient() {
        MutableClock clock = new MutableClock();
        TokenBucketRateLimiterService service = new TokenBucketRateLimiterService(1, 1, clock);

        assertThat(service.allowRequest("client-1").allowed()).isTrue();
        assertThat(service.allowRequest("client-1").allowed()).isFalse();
        assertThat(service.allowRequest("client-2").allowed()).isTrue();
    }

    private static final class MutableClock extends Clock {

        private Instant instant = Instant.parse("2026-01-01T00:00:00Z");

        void advanceMillis(long millis) {
            instant = instant.plusMillis(millis);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
