package com.smartsparrow.la.data;

import com.google.common.collect.ImmutableList;

public enum StatType {
    MIN,
    MAX,
    MEAN,
    MEDIAN;

    private static ImmutableList<StatType> progressStats = ImmutableList.of(MIN, MAX, MEAN, MEDIAN);

    /**
     * Helper method to check if a {@link StatType} is a progress stat type
     *
     * @param type the type to check
     * @return <code>true</code> if it matches a progress type else <code>false</code>
     */
    public static boolean isProgressStat(StatType type) {
        return progressStats.contains(type);
    }
}
