package com.smartsparrow.ext_http.service;

public interface Notification {

    /**
     * Get the notification state, which is carried across the ext-http flows.
     *
     * @return the notification state
     */
    NotificationState getState();
}
