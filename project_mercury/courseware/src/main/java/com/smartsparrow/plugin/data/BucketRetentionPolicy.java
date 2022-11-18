package com.smartsparrow.plugin.data;

/**
 * Provide the retention policy values for LogBucket
*/
public enum BucketRetentionPolicy {
    WEEK(7),
    FORTNIGHT(14),
    MONTH(30);

    private final int days;

    BucketRetentionPolicy(int days) {
        this.days = days;
    }

    private int days() {
        return days;
    }
}
