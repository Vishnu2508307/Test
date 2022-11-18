package com.smartsparrow.rtm.subscription.ingestion;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
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

import com.smartsparrow.ingestion.data.IngestionStatus;
import com.smartsparrow.ingestion.subscription.IngestionBroadcastMessage;
import com.smartsparrow.ingestion.subscription.IngestionConsumable;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.UUIDs;

class IngestionRTMConsumerTest {

    @InjectMocks
    private IngestionRTMConsumer ingestionRTMConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private IngestionConsumable ingestionConsumable;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private IngestionBroadcastMessage message;

    private static final String broadcastType = "project.ingest.broadcast";
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final UUID ingestionId = UUIDs.timeBased();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClient.getRtmClientContext()).thenReturn(rtmClientContext);
        when(rtmClientContext.getClientId()).thenReturn("clientId");
        when(rtmClient.getSession()).thenReturn(session);

        when(ingestionConsumable.getContent()).thenReturn(message);
        when(ingestionConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(ingestionConsumable.getSubscriptionId()).thenReturn(subscriptionId);

        when(message.getIngestionId()).thenReturn(ingestionId);
    }

    @Test
    @DisplayName("It should not filter out the same client producer")
    void accept_filterOutSameClientProducer() {
        ingestionRTMConsumer.accept(rtmClient, ingestionConsumable);

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(anyString());
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket")
    void accept_writesToWebSocket() {
        RTMClientContext producer = mock(RTMClientContext.class);
        when(producer.getClientId()).thenReturn("differentClientId");

        ingestionRTMConsumer.accept(rtmClient, ingestionConsumable);

        final String expected = "{" +
                "\"type\":\"project.ingest.broadcast\"," +
                "\"response\":{" +
                "\"rtmEvent\":\"PROJECT_INGESTION\"," +
                "\"action\":\"PROJECT_INGESTION\"," +
                "\"ingestionId\":\"" + ingestionId + "\"," +
                "\"ingestionStatus\":\"" + IngestionStatus.COMPLETED + "\"" +
                "}," +
                "\"replyTo\":\"" + subscriptionId + "\"}";

        assertNotNull(session.getRemote().sendStringByFuture(expected));

        verify(session.getRemote()).sendStringByFuture(expected);
    }
}
