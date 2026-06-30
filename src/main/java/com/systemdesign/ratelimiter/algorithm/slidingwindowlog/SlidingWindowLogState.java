package com.systemdesign.ratelimiter.algorithm.slidingwindowlog;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Mutable per-client state for the sliding window log algorithm.
 */
final class SlidingWindowLogState {

    /**
     * Accepted request timestamps retained for the active sliding window.
     */
    private final Deque<Long> requestTimestampsMillis = new ArrayDeque<>();

    /**
     * Adds an accepted request timestamp to the log.
     *
     * @param timestampMillis accepted request timestamp in milliseconds
     */
    void addRequest(long timestampMillis) {
        requestTimestampsMillis.addLast(timestampMillis);
    }

    /**
     * Removes request timestamps that are older than the active sliding window.
     *
     * @param cutoffMillis oldest timestamp that still belongs to the active window
     */
    void removeExpiredRequests(long cutoffMillis) {
        while (!requestTimestampsMillis.isEmpty() && requestTimestampsMillis.peekFirst() <= cutoffMillis) {
            requestTimestampsMillis.removeFirst();
        }
    }

    /**
     * Returns the number of accepted requests retained in the active sliding window.
     *
     * @return active request count
     */
    int requestCount() {
        return requestTimestampsMillis.size();
    }

    /**
     * Returns the oldest retained request timestamp, or zero when the log is empty.
     *
     * @return oldest retained request timestamp in milliseconds
     */
    long oldestRequestMillis() {
        Long oldestRequestMillis = requestTimestampsMillis.peekFirst();
        return oldestRequestMillis == null ? 0 : oldestRequestMillis;
    }
}
