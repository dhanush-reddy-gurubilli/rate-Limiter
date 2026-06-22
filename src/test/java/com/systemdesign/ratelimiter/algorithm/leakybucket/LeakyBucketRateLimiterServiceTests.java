package com.systemdesign.ratelimiter.algorithm.leakybucket;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

class LeakyBucketRateLimiterServiceTests {

    @Test
    void allowsRequestsUntilBucketCapacityIsReached() {
        MutableClock clock = new MutableClock();
        LeakyBucketRateLimiterService service = new LeakyBucketRateLimiterService(2, 1, clock);

        assertThat(service.allowRequest("client-1").allowed()).isTrue();
        assertThat(service.allowRequest("client-1").allowed()).isTrue();
        assertThat(service.allowRequest("client-1").allowed()).isFalse();
    }

    @Test
    void leaksRequestsOverTime() {
        MutableClock clock = new MutableClock();
        LeakyBucketRateLimiterService service = new LeakyBucketRateLimiterService(2, 1, clock);

        service.allowRequest("client-1");
        service.allowRequest("client-1");

        clock.advanceMillis(1_000);

        LeakyBucketDecision decision = service.allowRequest("client-1");

        assertThat(decision.allowed()).isTrue();
        assertThat(decision.currentWaterLevel()).isEqualTo(2);
    }

    @Test
    void keepsSeparateBucketsPerClient() {
        MutableClock clock = new MutableClock();
        LeakyBucketRateLimiterService service = new LeakyBucketRateLimiterService(1, 1, clock);

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
