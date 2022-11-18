package com.smartsparrow.workspace.data;

import com.google.common.collect.Lists;

public enum AlfrescoAssetSyncStatus {
    IN_PROGRESS,
    FAILED,
    COMPLETED;

    public static boolean isCompleted(final AlfrescoAssetSyncStatus trackStatus) {
        return Lists.newArrayList(FAILED, COMPLETED)
                .stream()
                .anyMatch(status -> status.equals(trackStatus));
    }
}
