package com.systemdesign.ratelimiter.algorithm.fixedwindow;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

class FixedWindowRateLimiterServiceTests {

    @Test
    void allowsRequestsUntilWindowLimitIsReached() {
        MutableClock clock = new MutableClock();
        FixedWindowRateLimiterService service = new FixedWindowRateLimiterService(2, 60, clock);

        assertThat(service.allowRequest("client-1").allowed()).isTrue();
        assertThat(service.allowRequest("client-1").allowed()).isTrue();
        assertThat(service.allowRequest("client-1").allowed()).isFalse();
    }

    @Test
    void resetsCountWhenNextWindowStarts() {
        MutableClock clock = new MutableClock();
        FixedWindowRateLimiterService service = new FixedWindowRateLimiterService(2, 60, clock);

        service.allowRequest("client-1");
        service.allowRequest("client-1");
        assertThat(service.allowRequest("client-1").allowed()).isFalse();

        clock.advanceMillis(60_000);

        FixedWindowDecision decision = service.allowRequest("client-1");

        assertThat(decision.allowed()).isTrue();
        assertThat(decision.requestCount()).isEqualTo(1);
        assertThat(decision.windowStartMillis()).isEqualTo(1_767_225_660_000L);
    }

    @Test
    void keepsSeparateWindowsPerClient() {
        MutableClock clock = new MutableClock();
        FixedWindowRateLimiterService service = new FixedWindowRateLimiterService(1, 60, clock);

        assertThat(service.allowRequest("client-1").allowed()).isTrue();
        assertThat(service.allowRequest("client-1").allowed()).isFalse();
        assertThat(service.allowRequest("client-2").allowed()).isTrue();
    }

    @Test
    void resetClientClearsStoredWindow() {
        MutableClock clock = new MutableClock();
        FixedWindowRateLimiterService service = new FixedWindowRateLimiterService(1, 60, clock);

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
