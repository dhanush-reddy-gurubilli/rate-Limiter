package com.systemdesign.ratelimiter.algorithm.leakybucket;

/**
 * Mutable state for one client's leaky bucket.
 */
final class BucketState {

    /**
     * Current amount of queued request water in the bucket.
     */
    private double currentWaterLevel;

    /**
     * Last time the bucket was updated after applying leakage.
     */
    private long lastUpdatedMillis;

    /**
     * Creates an empty bucket state initialized at the supplied timestamp.
     *
     * @param lastUpdatedMillis timestamp used as the initial leak reference point
     */
    BucketState(long lastUpdatedMillis) {
        this.lastUpdatedMillis = lastUpdatedMillis;
    }

    /**
     * Returns the current bucket water level.
     *
     * @return current queued request amount
     */
    double currentWaterLevel() {
        return currentWaterLevel;
    }

    /**
     * Returns the timestamp of the last leak calculation.
     *
     * @return last update timestamp in milliseconds
     */
    long lastUpdatedMillis() {
        return lastUpdatedMillis;
    }

    /**
     * Applies leaked request capacity and moves the update timestamp forward.
     *
     * @param leakedRequests amount of water to drain from the bucket
     * @param nowMillis timestamp used for this update
     */
    void leak(double leakedRequests, long nowMillis) {
        currentWaterLevel = Math.max(0, currentWaterLevel - leakedRequests);
        lastUpdatedMillis = nowMillis;
    }

    /**
     * Adds one accepted request to the bucket.
     */
    void addRequest() {
        currentWaterLevel++;
    }
}
