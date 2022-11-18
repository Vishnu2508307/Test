package com.smartsparrow.rtm.message.handler.cohort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.recv.cohort.CohortGenericMessage;
import com.smartsparrow.rtm.subscription.SubscriptionLimitExceeded;
import com.smartsparrow.rtm.subscription.cohort.CohortRTMSubscription;
import com.smartsparrow.rtm.subscription.data.RTMSubscription;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;

import reactor.core.publisher.Mono;

class CohortSubscribeMessageHandlerTest {

    @Mock
    private Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;
    @Mock
    private RTMSubscriptionManager rtmSubscriptionManager;
    @Mock
    private CohortRTMSubscription cohortRTMSubscription;
    @Mock
    private CohortRTMSubscription.CohortRTMSubscriptionFactory cohortRTMSubscriptionFactory;
    @InjectMocks
    private CohortSubscribeMessageHandler cohortSubscribeMessageHandler;
    private Session session;
    private static UUID cohortId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmSubscriptionManagerProvider.get()).thenReturn(rtmSubscriptionManager);
        session = RTMWebSocketTestUtils.mockSession();
        when(rtmSubscriptionManager.add(any(RTMSubscription.class))).thenReturn(Mono.just(1));
        when(cohortRTMSubscription.getName()).thenReturn("workspace.cohort/subscription/" + cohortId);
        when(cohortRTMSubscription.getId()).thenReturn(UUID.randomUUID());

        cohortSubscribeMessageHandler = new CohortSubscribeMessageHandler(rtmSubscriptionManagerProvider, cohortRTMSubscriptionFactory);
        cohortRTMSubscription = new CohortRTMSubscription(cohortId);
        when(cohortRTMSubscriptionFactory.create(cohortId)).thenReturn(cohortRTMSubscription);
    }

    @Test
    void validate_noCohortId() {
        RTMValidationException t = assertThrows(RTMValidationException.class,
                () -> cohortSubscribeMessageHandler.validate(new CohortGenericMessage()));

        assertEquals("workspace.cohort.subscribe.error", t.getType());
        assertEquals(400, t.getStatusCode());
        assertEquals("cohortId is required", t.getErrorMessage());
    }

    @Test
    void handle_success() throws Exception {
        CohortGenericMessage message = mock(CohortGenericMessage.class);
        when(message.getCohortId()).thenReturn(cohortId);

        cohortSubscribeMessageHandler.handle(session, message);

        ArgumentCaptor<CohortRTMSubscription> captor = ArgumentCaptor.forClass(CohortRTMSubscription.class);
        verify(rtmSubscriptionManager).add(captor.capture());
        assertEquals("workspace.cohort/subscription/" + cohortId, captor.getValue().getName());

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"workspace.cohort.subscribe.ok\"," +
                "\"response\":{\"rtmSubscriptionId\":\"" + captor.getValue().getId() + "\"}}");

        verify(rtmSubscriptionManager).add(cohortRTMSubscription);
    }

    @Test
    void handle_limitExceeded() throws Exception {
        UUID cohortId = UUID.randomUUID();
        CohortGenericMessage message = mock(CohortGenericMessage.class);
        when(message.getCohortId()).thenReturn(cohortId);

        when(rtmSubscriptionManager.add(any())).thenReturn(Mono.error(new SubscriptionLimitExceeded()));
        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> cohortSubscribeMessageHandler.handle(session, message));
        assertEquals("Maximum number of subscriptions reached", t.getMessage());
    }
}
