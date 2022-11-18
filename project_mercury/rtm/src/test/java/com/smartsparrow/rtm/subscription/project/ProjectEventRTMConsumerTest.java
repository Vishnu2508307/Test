package com.smartsparrow.rtm.subscription.project;

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
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.workspace.subscription.ProjectBroadcastMessage;
import com.smartsparrow.workspace.subscription.ProjectEventConsumable;

class ProjectEventRTMConsumerTest {

    @InjectMocks
    private ProjectEventRTMConsumer projectEventRTMConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private ProjectEventConsumable projectEventConsumable;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private ProjectBroadcastMessage message;

    private static final String broadcastType = "workspace.project.broadcast";
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final UUID projectId = UUIDs.timeBased();
    private static final UUID ingestionId = UUIDs.timeBased();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClient.getRtmClientContext()).thenReturn(rtmClientContext);
        when(rtmClientContext.getClientId()).thenReturn("clientId");
        when(rtmClient.getSession()).thenReturn(session);

        when(projectEventConsumable.getContent()).thenReturn(message);
        when(projectEventConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(projectEventConsumable.getSubscriptionId()).thenReturn(subscriptionId);

        when(message.getProjectId()).thenReturn(projectId);
        when(message.getIngestionId()).thenReturn(ingestionId);
        when(message.getIngestionStatus()).thenReturn(IngestionStatus.COMPLETED);
    }

    @Test
    @DisplayName("It should not filter out the same client producer")
    void accept_filterOutSameClientProducer() {

        projectEventRTMConsumer.accept(rtmClient, projectEventConsumable);

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(anyString());
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket")
    void accept_writesToWebSocket() {
        RTMClientContext producer = mock(RTMClientContext.class);
        when(producer.getClientId()).thenReturn("differentClientId");

        projectEventRTMConsumer.accept(rtmClient, projectEventConsumable);

        final String expected = "{" +
                "\"type\":\"" + broadcastType + "\"," +
                "\"response\":{" +
                "\"rtmEvent\":\"PROJECT_EVENT\"," +
                "\"projectId\":\"" + projectId + "\"," +
                "\"ingestionId\":\"" + ingestionId + "\"," +
                "\"ingestionStatus\":\"" + IngestionStatus.COMPLETED + "\"" +
                "}," +
                "\"replyTo\":\"" + subscriptionId + "\"}";

        assertNotNull(session.getRemote().sendStringByFuture(expected));

        verify(session.getRemote()).sendStringByFuture(expected);
    }
}
