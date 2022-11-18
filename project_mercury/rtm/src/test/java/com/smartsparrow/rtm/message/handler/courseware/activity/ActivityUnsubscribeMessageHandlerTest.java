package com.smartsparrow.rtm.message.handler.courseware.activity;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.courseware.activity.ActivityGenericMessage;
import com.smartsparrow.rtm.subscription.SubscriptionNotFound;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription.ActivityRTMSubscriptionFactory;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;

class ActivityUnsubscribeMessageHandlerTest {

    @InjectMocks
    private ActivityUnsubscribeMessageHandler handler;

    @Mock
    private Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;

    @Mock
    private RTMSubscriptionManager subscriptionManager;

    @Mock
    private ActivityRTMSubscription activityRTMSubscription;

    @Mock
    private ActivityRTMSubscriptionFactory activityRTMSubscriptionFactory;

    @Mock
    private ActivityGenericMessage message;

    private Session session;
    private static final UUID activityId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = RTMWebSocketTestUtils.mockSession();

        when(rtmSubscriptionManagerProvider.get()).thenReturn(subscriptionManager);
        when(activityRTMSubscriptionFactory.create(activityId)).thenReturn(new ActivityRTMSubscription(activityId));
        when(message.getActivityId()).thenReturn(activityId);
        when(activityRTMSubscription.getName()).thenReturn(ActivityRTMSubscription.NAME(activityId));

        handler = new ActivityUnsubscribeMessageHandler(rtmSubscriptionManagerProvider, activityRTMSubscriptionFactory);
    }

    @Test
    void validate_missingActivityId() {
        when(message.getActivityId()).thenReturn(null);
        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertAll(()->{
            assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
            assertEquals("author.activity.unsubscribe.error", e.getType());
            assertEquals("activityId is required", e.getErrorMessage());
        });
    }

    @Test
    void handle_subscriptionNotFound() throws SubscriptionNotFound, WriteResponseException {
        doThrow(SubscriptionNotFound.class).when(subscriptionManager).unsubscribe(anyString());

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"author.activity.unsubscribe.error\"," +
                "\"code\":" + HttpStatus.SC_NOT_FOUND + "," +
                "\"message\":\"" + String.format("Subscription for activity %s not found", message.getActivityId()) +
                "\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle() throws WriteResponseException, SubscriptionNotFound {
        handler.handle(session, message);
        String expected = "{\"type\":\"author.activity.unsubscribe.ok\"}";

        verify(subscriptionManager, atLeastOnce()).unsubscribe(anyString());
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}
