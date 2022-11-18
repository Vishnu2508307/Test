package com.smartsparrow.rtm.message.handler.courseware.activity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.recv.courseware.activity.ActivityGenericMessage;
import com.smartsparrow.rtm.subscription.SubscriptionAlreadyExists;
import com.smartsparrow.rtm.subscription.SubscriptionLimitExceeded;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription;
import com.smartsparrow.rtm.subscription.courseware.ActivityRTMSubscription.ActivityRTMSubscriptionFactory;
import com.smartsparrow.rtm.subscription.data.RTMSubscription;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;

import reactor.core.publisher.Mono;

class ActivitySubscribeMessageHandlerTest {

    @Mock
    private Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;

    @Mock
    private RTMSubscriptionManager subscriptionManager;

    @Mock
    private ActivityRTMSubscription activityRTMSubscription;

    @Mock
    private ActivityRTMSubscriptionFactory activityRTMSubscriptionFactory;

    private ActivitySubscribeMessageHandler activitySubscribeMessageHandler;

    @Mock
    ActivityGenericMessage message;

    private Session session;
    private static final UUID activityId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmSubscriptionManagerProvider.get()).thenReturn(subscriptionManager);
        session = RTMWebSocketTestUtils.mockSession();

        when(subscriptionManager.add(any(RTMSubscription.class))).thenReturn(Mono.just(1));
        when(message.getActivityId()).thenReturn(activityId);

        activitySubscribeMessageHandler = new ActivitySubscribeMessageHandler(rtmSubscriptionManagerProvider, activityRTMSubscriptionFactory);
        activityRTMSubscription = new ActivityRTMSubscription(activityId);
        when(activityRTMSubscriptionFactory.create(activityId)).thenReturn(activityRTMSubscription);

    }

    @Test
    void validate_noActivityId() {
        when(message.getActivityId()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class,
                () -> activitySubscribeMessageHandler.validate(new ActivityGenericMessage()));

        assertEquals("author.activity.subscribe.error", t.getType());
        assertEquals(400, t.getStatusCode());
        assertEquals("activityId is required", t.getErrorMessage());
    }

    @Test
    void handle_success() throws Exception {
        activitySubscribeMessageHandler.handle(session, message);

        verify(subscriptionManager).add(activityRTMSubscription);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"author.activity.subscribe.ok\"," +
                "\"response\":{\"rtmSubscriptionId\":\"" + activityRTMSubscription.getId() + "\"}}");
    }

    @Test
    void handle_subscriptionLimitError() {
        when(subscriptionManager.add(any())).thenReturn(Mono.error(new SubscriptionLimitExceeded()));

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () ->
                activitySubscribeMessageHandler.handle(session, mock(ActivityGenericMessage.class)));

        assertEquals("Maximum number of subscriptions reached", t.getMessage());
    }

    @Test
    void handle_subscriptionExistsError() {
        when(subscriptionManager.add(any())).thenReturn(Mono.error(new SubscriptionAlreadyExists()));

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () ->
                activitySubscribeMessageHandler.handle(session, mock(ActivityGenericMessage.class)));

        assertEquals("Subscription already exists", t.getMessage());
    }
}
