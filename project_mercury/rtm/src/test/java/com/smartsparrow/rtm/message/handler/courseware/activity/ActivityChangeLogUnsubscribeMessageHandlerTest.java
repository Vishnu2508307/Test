package com.smartsparrow.rtm.message.handler.courseware.activity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.courseware.activity.ActivityGenericMessage;
import com.smartsparrow.rtm.subscription.SubscriptionManager;
import com.smartsparrow.rtm.subscription.SubscriptionNotFound;

class ActivityChangeLogUnsubscribeMessageHandlerTest {

    @Mock
    private Provider<SubscriptionManager> subscriptionManagerProvider;

    @Mock
    private SubscriptionManager subscriptionManager;

    @Mock
    private ActivityGenericMessage message;

    @InjectMocks
    private ActivityChangeLogUnsubscribeMessageHandler handler;

    private static final UUID activityId = UUID.randomUUID();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(subscriptionManagerProvider.get()).thenReturn(subscriptionManager);
        when(message.getActivityId()).thenReturn(activityId);
    }

    @Test
    void validate_noProjectId() {
        when(message.getActivityId()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(f);
        assertEquals("activityId is required", f.getMessage());
    }

    @Test
    void validate() {
        assertDoesNotThrow(() -> handler.validate(message));
    }

    @Test
    void handle_subscriptionNotFound() throws SubscriptionNotFound, WriteResponseException {
        doThrow(SubscriptionNotFound.class).when(subscriptionManager).unsubscribe(anyString());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"project.activity.changelog.unsubscribe.error\"," +
                "\"code\":404," +
                "\"message\":\"Changelog Subscription for activity "+ activityId +" not found\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle() throws WriteResponseException, SubscriptionNotFound {
        handler.handle(session, message);

        verify(subscriptionManager).unsubscribe(anyString());

        String expected = "{\"type\":\"project.activity.changelog.unsubscribe.ok\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}