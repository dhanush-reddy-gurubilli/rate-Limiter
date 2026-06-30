package com.systemdesign.ratelimiter.algorithm.slidingwindowlog;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

class SlidingWindowLogRateLimiterServiceTests {

    @Test
    void allowsRequestsUntilWindowLimitIsReached() {
        MutableClock clock = new MutableClock();
        SlidingWindowLogRateLimiterService service = new SlidingWindowLogRateLimiterService(2, 60, clock);

        assertThat(service.allowRequest("client-1").allowed()).isTrue();
        assertThat(service.allowRequest("client-1").allowed()).isTrue();
        assertThat(service.allowRequest("client-1").allowed()).isFalse();
    }

    @Test
    void removesExpiredRequestsWhenWindowSlides() {
        MutableClock clock = new MutableClock();
        SlidingWindowLogRateLimiterService service = new SlidingWindowLogRateLimiterService(2, 60, clock);

        service.allowRequest("client-1");
        clock.advanceMillis(30_000);
        service.allowRequest("client-1");
        assertThat(service.allowRequest("client-1").allowed()).isFalse();

        clock.advanceMillis(30_001);

        SlidingWindowLogDecision decision = service.allowRequest("client-1");

        assertThat(decision.allowed()).isTrue();
        assertThat(decision.requestCount()).isEqualTo(2);
        assertThat(decision.oldestRequestMillis()).isEqualTo(1_767_225_630_000L);
    }

    @Test
    void keepsSeparateLogsPerClient() {
        MutableClock clock = new MutableClock();
        SlidingWindowLogRateLimiterService service = new SlidingWindowLogRateLimiterService(1, 60, clock);

        assertThat(service.allowRequest("client-1").allowed()).isTrue();
        assertThat(service.allowRequest("client-1").allowed()).isFalse();
        assertThat(service.allowRequest("client-2").allowed()).isTrue();
    }

    @Test
    void resetClientClearsStoredLog() {
        MutableClock clock = new MutableClock();
        SlidingWindowLogRateLimiterService service = new SlidingWindowLogRateLimiterService(1, 60, clock);

        assertThat(service.allowRequest("client-1").allowed()).isTrue();
        assertThat(service.allowRequest("client-1").allowed()).isFalse();

        service.resetClient("client-1");

        assertThat(service.allowRequest("client-1").allowed()).isTrue();
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
