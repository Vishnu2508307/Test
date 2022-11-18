package com.smartsparrow.export.data;

import java.util.UUID;

public interface Notification {

    UUID getNotificationId();

    ExportStatus getStatus();
}
