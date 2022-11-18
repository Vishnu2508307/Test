package com.smartsparrow.export.service;

import java.util.UUID;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.export.data.ExportAmbrosiaSnippet;
import com.smartsparrow.export.data.ExportErrorNotification;
import com.smartsparrow.export.data.ExportRequestNotification;
import com.smartsparrow.export.data.ExportRetryNotification;
import com.smartsparrow.export.data.ExportStatus;

public class ExportTestStub {

    public static ExportRequestNotification buildRequestNotification(final CoursewareElement coursewareElement,
                                                                     final UUID projectId, final UUID workspaceId,
                                                                     final UUID accountId, final UUID exportId) {
        return new ExportRequestNotification(coursewareElement.getElementId())
                .setAccountId(accountId)
                .setElementType(coursewareElement.getElementType())
                .setNotificationId(UUID.randomUUID())
                .setProjectId(projectId)
                .setStatus(ExportStatus.IN_PROGRESS)
                .setWorkspaceId(workspaceId)
                .setExportId(exportId);
    }

    public static ExportAmbrosiaSnippet buildResultNotification(final ExportRequestNotification notification) {
        return new ExportAmbrosiaSnippet()
                .setAccountId(notification.getAccountId())
                .setElementId(notification.getElementId())
                .setElementType(notification.getElementType())
                .setNotificationId(notification.getNotificationId())
                .setExportId(notification.getExportId());
    }

    public static ExportErrorNotification buildErrorNotification(final String cause, final String error,
                                                                 final UUID notificationId, final UUID exportId) {
        return new ExportErrorNotification()
                .setCause(cause)
                .setErrorMessage(error)
                .setNotificationId(notificationId)
                .setExportId(exportId);
    }

    public static ExportRetryNotification buildRetryNotification(final UUID notificationId) {
        return new ExportRetryNotification()
                .setDelaySec(null)
                .setNotificationId(notificationId)
                .setSourceNotificationId(notificationId);
    }
}
