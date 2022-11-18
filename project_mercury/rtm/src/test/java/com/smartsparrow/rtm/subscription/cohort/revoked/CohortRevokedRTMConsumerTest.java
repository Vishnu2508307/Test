package com.smartsparrow.rtm.subscription.cohort.revoked;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.cohort.eventmessage.CohortBroadcastMessage;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.UUIDs;

class CohortRevokedRTMConsumerTest {

    @InjectMocks
    private CohortRevokedRTMConsumer cohortRevokedRTMConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private CohortRevokedRTMConsumable cohortRevokedRTMConsumable;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private CohortBroadcastMessage message;

    private static final String broadcastType = "workspace.cohort.broadcast";
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final UUID cohortId = UUIDs.timeBased();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClient.getRtmClientContext()).thenReturn(rtmClientContext);
        when(rtmClientContext.getClientId()).thenReturn("clientId");
        when(rtmClient.getSession()).thenReturn(session);

        when(cohortRevokedRTMConsumable.getContent()).thenReturn(message);
        when(cohortRevokedRTMConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(cohortRevokedRTMConsumable.getSubscriptionId()).thenReturn(subscriptionId);

        when(message.getCohortId()).thenReturn(cohortId);
    }

    @Test
    @DisplayName("It should filter out the same client producer")
    void accept_filterOutSameClientProducer() {
        when(cohortRevokedRTMConsumable.getRTMClientContext()).thenReturn(rtmClientContext);

        cohortRevokedRTMConsumer.accept(rtmClient, cohortRevokedRTMConsumable);

        verify(session.getRemote(), never()).sendStringByFuture(anyString());
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket")
    void accept_writesToWebSocket() {
        RTMClientContext producer = mock(RTMClientContext.class);
        when(producer.getClientId()).thenReturn("differentClientId");
        when(cohortRevokedRTMConsumable.getRTMClientContext()).thenReturn(producer);

        cohortRevokedRTMConsumer.accept(rtmClient, cohortRevokedRTMConsumable);

        final String expected = "{" +
                "\"type\":\"workspace.cohort.broadcast\"," +
                "\"response\":{" +
                "\"rtmEvent\":\"COHORT_REVOKED\"," +
                "\"action\":\"REVOKED\"," +
                "\"cohortId\":\"" + cohortId + "\"" +
                "}," +
                "\"replyTo\":\"" + subscriptionId + "\"}";

        assertNotNull(session.getRemote().sendStringByFuture(expected));

        verify(session.getRemote()).sendStringByFuture(expected);
    }
}
