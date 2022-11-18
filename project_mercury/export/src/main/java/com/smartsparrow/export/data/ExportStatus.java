package com.smartsparrow.export.data;


import com.google.common.collect.Lists;

public enum ExportStatus {
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    RETRY_RECEIVED,
    RETRY_DELAY_SUBMITTED;

    public static boolean isCompleted(final ExportStatus exportStatus) {
        return Lists.newArrayList(COMPLETED, FAILED)
                .stream()
                .anyMatch(status -> status.equals(exportStatus));
    }
}
