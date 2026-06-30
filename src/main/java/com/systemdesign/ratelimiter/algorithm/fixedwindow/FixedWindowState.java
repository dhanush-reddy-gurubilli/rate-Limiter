package com.systemdesign.ratelimiter.algorithm.fixedwindow;

/**
 * Mutable state for one client's fixed window counter.
 */
final class FixedWindowState {

    /**
     * Start timestamp of the active fixed window.
     */
    private long windowStartMillis;

    /**
     * Number of accepted requests in the active fixed window.
     */
    private int requestCount;

    /**
     * Creates fixed window state initialized at the supplied timestamp.
     *
     * @param windowStartMillis start timestamp for the initial window
     */
    FixedWindowState(long windowStartMillis) {
        this.windowStartMillis = windowStartMillis;
        this.requestCount = 0;
    }

    /**
     * Returns the start timestamp of the active window.
     *
     * @return window start timestamp in milliseconds
     */
    long windowStartMillis() {
        return windowStartMillis;
    }

    /**
     * Returns the number of accepted requests in the active window.
     *
     * @return accepted request count
     */
    int requestCount() {
        return requestCount;
    }

    /**
     * Moves this client into a new window and clears the request count.
     *
     * @param windowStartMillis start timestamp for the new window
     */
    void resetWindow(long windowStartMillis) {
        this.windowStartMillis = windowStartMillis;
        this.requestCount = 0;
    }

    /**
     * Records one accepted request in the active window.
     */
    void incrementRequestCount() {
        requestCount++;
    }
}
