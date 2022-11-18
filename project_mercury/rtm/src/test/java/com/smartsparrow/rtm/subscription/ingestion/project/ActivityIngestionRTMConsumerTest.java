package com.smartsparrow.rtm.subscription.ingestion.project;

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
import com.smartsparrow.pubsub.subscriptions.activityIngestion.ActivityIngestionBroadcastMessage;
import com.smartsparrow.pubsub.subscriptions.activityIngestion.ActivityIngestionConsumable;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.UUIDs;

class ActivityIngestionRTMConsumerTest {

    @InjectMocks
    private ActivityIngestionRTMConsumer activityIngestionRTMConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private ActivityIngestionConsumable activityIngestionConsumable;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private ActivityIngestionBroadcastMessage message;

    private static final String broadcastType = "author.activity.broadcast";
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final UUID ingestionId = UUIDs.timeBased();
    private static final UUID projectId = UUIDs.timeBased();
    private static final UUID rootElementId = UUIDs.timeBased();
    private static final IngestionStatus ingestionStatus = IngestionStatus.UPLOADED;
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClient.getRtmClientContext()).thenReturn(rtmClientContext);
        when(rtmClientContext.getClientId()).thenReturn("clientId");
        when(rtmClient.getSession()).thenReturn(session);

        when(activityIngestionConsumable.getContent()).thenReturn(message);
        when(activityIngestionConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(activityIngestionConsumable.getSubscriptionId()).thenReturn(subscriptionId);

        when(message.getIngestionId()).thenReturn(ingestionId);
        when(message.getProjectId()).thenReturn(projectId);
        when(message.getRootElementId()).thenReturn(rootElementId);
        when(message.getIngestionStatus()).thenReturn(ingestionStatus);
    }

    @Test
    @DisplayName("It should not filter out the same client producer")
    void accept_filterOutSameClientProducer() {
        activityIngestionRTMConsumer.accept(rtmClient, activityIngestionConsumable);

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(anyString());
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket")
    void accept_writesToWebSocket() {
        RTMClientContext producer = mock(RTMClientContext.class);
        when(producer.getClientId()).thenReturn("differentClientId");

        activityIngestionRTMConsumer.accept(rtmClient, activityIngestionConsumable);

        final String expected = "{" +
                "\"type\":\"author.activity.broadcast\"," +
                "\"response\":{" +
                "\"rtmEvent\":\"ACTIVITY_INGESTION\"," +
                "\"action\":\"ACTIVITY_INGESTION\"," +
                "\"ingestionId\":\"" + ingestionId + "\"," +
                "\"projectId\":\"" + projectId + "\"," +
                "\"rootElementId\":\"" + rootElementId + "\"," +
                "\"ingestionStatus\":\"" + ingestionStatus + "\"" +
                "}," +
                "\"replyTo\":\"" + subscriptionId + "\"}";

        assertNotNull(session.getRemote().sendStringByFuture(expected));

        verify(session.getRemote()).sendStringByFuture(expected);
    }
}
