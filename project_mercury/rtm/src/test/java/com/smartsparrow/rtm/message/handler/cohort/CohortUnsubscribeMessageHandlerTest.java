package com.smartsparrow.rtm.message.handler.cohort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.recv.cohort.CohortGenericMessage;
import com.smartsparrow.rtm.subscription.SubscriptionNotFound;
import com.smartsparrow.rtm.subscription.cohort.CohortRTMSubscription;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.workspace.WorkspaceRTMSubscription;
import com.smartsparrow.util.UUIDs;

class CohortUnsubscribeMessageHandlerTest {

    private CohortUnsubscribeMessageHandler handler;
    @Mock
    private Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;
    @Mock
    private RTMSubscriptionManager rtmSubscriptionManager;
    @Mock
    private CohortRTMSubscription cohortRTMSubscription;
    @Mock
    private CohortRTMSubscription.CohortRTMSubscriptionFactory cohortRTMSubscriptionFactory;
    @Mock
    private CohortGenericMessage message;

    private static final UUID cohortId = UUIDs.timeBased();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmSubscriptionManagerProvider.get()).thenReturn(rtmSubscriptionManager);
        when(message.getCohortId()).thenReturn(cohortId);
        when(cohortRTMSubscription.getName()).thenReturn(WorkspaceRTMSubscription.NAME(cohortId));
        when(cohortRTMSubscriptionFactory.create(cohortId)).thenReturn(new CohortRTMSubscription(cohortId));

        handler = new CohortUnsubscribeMessageHandler(rtmSubscriptionManagerProvider, cohortRTMSubscriptionFactory);
    }

    @Test
    void validate_noCohortId() {
        when(message.getCohortId()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("cohortId is required", f.getMessage());
    }

    @Test
    void handle_success() throws Exception {
        handler.handle(session, message);

        final String expected = "{\"type\":\"workspace.cohort.unsubscribe.ok\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(rtmSubscriptionManager).unsubscribe(cohortRTMSubscriptionFactory.create(cohortId).getName());
    }

    @Test
    void handle_subscriptionNotFound() throws Exception {
        doThrow(new SubscriptionNotFound("test")).when(rtmSubscriptionManager).unsubscribe(any(String.class));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"workspace.cohort.unsubscribe.error\"," +
                "\"code\":404,\"message\":\"Subscription for cohort " + cohortId + " not found\"}");
    }
}
