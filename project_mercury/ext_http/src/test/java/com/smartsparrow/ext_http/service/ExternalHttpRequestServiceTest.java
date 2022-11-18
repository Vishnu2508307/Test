package com.smartsparrow.ext_http.service;


import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.util.UUIDs;

class ExternalHttpRequestServiceTest {

    @InjectMocks
    private ExternalHttpRequestService externalHttpRequestService;

    @BeforeEach
    void setup() {
        // create any @Mock
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void submit_invalidParams() {
        final RequestPurpose purpose = RequestPurpose.GENERAL;
        final Request request = new Request().setUri("https://httpbin.org/status/200");
        final Request reqNullUri = new Request().setUri(null);
        final Request reqUnsetUri = new Request();
        final UUID refId = UUIDs.timeBased();

        assertThrows(IllegalArgumentFault.class, () -> externalHttpRequestService.submit(null, request, refId));
        assertThrows(IllegalArgumentFault.class, () -> externalHttpRequestService.submit(purpose, null, refId));

        assertThrows(IllegalArgumentFault.class, () -> externalHttpRequestService.submit(purpose, reqNullUri, refId));
        assertThrows(IllegalArgumentFault.class, () -> externalHttpRequestService.submit(purpose, reqUnsetUri, refId));
    }

    @Test
    void scheduleRetry_invalidParams() {
        final Notification n = () -> new NotificationState()
                .setNotificationId(UUIDs.timeBased())
                .setPurpose(RequestPurpose.GENERAL);
        final Notification stateNull = () -> null;
        final Notification stateNoPurpose = () -> new NotificationState().setNotificationId(UUIDs.timeBased());
        final Notification stateNoId = () -> new NotificationState().setPurpose(RequestPurpose.GENERAL);
        //
        final Duration delay = Duration.ofMinutes(5);
        final Duration negativeDelay = Duration.ofMinutes(-5);
        final Duration zeroDelay = Duration.ofMinutes(0);

        assertThrows(IllegalArgumentFault.class, () -> externalHttpRequestService.scheduleRetry(null, delay));
        assertThrows(IllegalArgumentFault.class, () -> externalHttpRequestService.scheduleRetry(n, null));

        assertThrows(IllegalArgumentFault.class, () -> externalHttpRequestService.scheduleRetry(stateNull, delay));
        assertThrows(IllegalArgumentFault.class, () -> externalHttpRequestService.scheduleRetry(stateNoPurpose, delay));
        assertThrows(IllegalArgumentFault.class, () -> externalHttpRequestService.scheduleRetry(stateNoId, delay));

        assertThrows(IllegalArgumentFault.class, () -> externalHttpRequestService.scheduleRetry(n, negativeDelay));
        assertThrows(IllegalArgumentFault.class, () -> externalHttpRequestService.scheduleRetry(n, zeroDelay));
    }

    @Test
    void processResultNotification_invalidParams() {
        assertThrows(IllegalArgumentFault.class, () -> externalHttpRequestService.processResultNotification(null));
    }

    @Test
    void processRetryNotification_invalidParams() {
        assertThrows(IllegalArgumentFault.class, () -> externalHttpRequestService.processRetryNotification(null));
    }

    @Test
    void processErrorNotification_invalidParams() {
        assertThrows(IllegalArgumentFault.class, () -> externalHttpRequestService.processErrorNotification(null));
    }
}