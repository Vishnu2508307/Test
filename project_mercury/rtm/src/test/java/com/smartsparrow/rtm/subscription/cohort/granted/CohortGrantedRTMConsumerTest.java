package com.smartsparrow.rtm.subscription.cohort.granted;

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

class CohortGrantedRTMConsumerTest {

    @InjectMocks
    private CohortGrantedRTMConsumer cohortGrantedRTMConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private CohortGrantedRTMConsumable cohortGrantedRTMConsumable;

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

        when(cohortGrantedRTMConsumable.getContent()).thenReturn(message);
        when(cohortGrantedRTMConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(cohortGrantedRTMConsumable.getSubscriptionId()).thenReturn(subscriptionId);

        when(message.getCohortId()).thenReturn(cohortId);
    }

    @Test
    @DisplayName("It should filter out the same client producer")
    void accept_filterOutSameClientProducer() {
        when(cohortGrantedRTMConsumable.getRTMClientContext()).thenReturn(rtmClientContext);

        cohortGrantedRTMConsumer.accept(rtmClient, cohortGrantedRTMConsumable);

        verify(session.getRemote(), never()).sendStringByFuture(anyString());
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket")
    void accept_writesToWebSocket() {
        RTMClientContext producer = mock(RTMClientContext.class);
        when(producer.getClientId()).thenReturn("differentClientId");
        when(cohortGrantedRTMConsumable.getRTMClientContext()).thenReturn(producer);

        cohortGrantedRTMConsumer.accept(rtmClient, cohortGrantedRTMConsumable);

        final String expected = "{" +
                "\"type\":\"workspace.cohort.broadcast\"," +
                "\"response\":{" +
                "\"rtmEvent\":\"COHORT_GRANTED\"," +
                "\"action\":\"GRANTED\"," +
                "\"cohortId\":\"" + cohortId + "\"" +
                "}," +
                "\"replyTo\":\"" + subscriptionId + "\"}";

        assertNotNull(session.getRemote().sendStringByFuture(expected));

        verify(session.getRemote()).sendStringByFuture(expected);
    }
}
