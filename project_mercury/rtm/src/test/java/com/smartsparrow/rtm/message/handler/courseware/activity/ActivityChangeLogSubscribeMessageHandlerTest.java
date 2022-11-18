package com.smartsparrow.rtm.message.handler.courseware.activity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
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
import com.smartsparrow.rtm.subscription.SubscriptionAlreadyExists;
import com.smartsparrow.rtm.subscription.SubscriptionLimitExceeded;
import com.smartsparrow.rtm.subscription.SubscriptionManager;
import com.smartsparrow.rtm.subscription.courseware.ActivityChangeLogSubscription;

import reactor.core.publisher.Mono;

class ActivityChangeLogSubscribeMessageHandlerTest {

    @Mock
    private Provider<SubscriptionManager> subscriptionManagerProvider;

    @Mock
    private SubscriptionManager subscriptionManager;

    @Mock
    private ActivityChangeLogSubscription activityChangeLogSubscription;

    @Mock
    private ActivityGenericMessage message;

    @InjectMocks
    private ActivityChangeLogSubscribeMessageHandler handler;

    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private static final UUID activityId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(subscriptionManagerProvider.get()).thenReturn(subscriptionManager);
        when(activityChangeLogSubscription.getId()).thenReturn("uniqId");
        when(subscriptionManager.add(any(ActivityChangeLogSubscription.class))).thenReturn(Mono.just(1));
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
    void handle_subscriptionLimitError() {
        when(subscriptionManager.add(any(ActivityChangeLogSubscription.class)))
                .thenReturn(Mono.error(new SubscriptionLimitExceeded()));

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.handle(session, message));
        assertEquals("Maximum number of subscriptions reached", f.getMessage());
    }

    @Test
    void handle_subscriptionExistsError() {
        when(subscriptionManager.add(any(ActivityChangeLogSubscription.class)))
                .thenReturn(Mono.error(new SubscriptionAlreadyExists()));

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () ->
                handler.handle(session, message));

        assertEquals("Subscription already exists", t.getMessage());
    }

    @Test
    void handle() throws WriteResponseException {
        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"project.activity.changelog.subscribe.ok\"," +
                "\"response\":{\"rtmSubscriptionId\":\"uniqId\"}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}