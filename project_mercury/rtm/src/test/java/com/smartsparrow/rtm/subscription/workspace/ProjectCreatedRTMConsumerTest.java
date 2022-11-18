package com.smartsparrow.rtm.subscription.workspace;

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

import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.UUIDs;

class ProjectCreatedRTMConsumerTest {

    @InjectMocks
    private ProjectCreatedRTMConsumer createdRTMConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private ProjectCreatedRTMConsumable projectCreatedRTMConsumable;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private ProjectCreatedBroadcastMessage message;

    private static final String broadcastType = "workspace.broadcast";
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final UUID projectId = UUIDs.timeBased();
    private static final UUID workspaceId = UUIDs.timeBased();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClient.getRtmClientContext()).thenReturn(rtmClientContext);
        when(rtmClientContext.getClientId()).thenReturn("clientId");
        when(rtmClient.getSession()).thenReturn(session);

        when(projectCreatedRTMConsumable.getContent()).thenReturn(message);
        when(projectCreatedRTMConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(projectCreatedRTMConsumable.getSubscriptionId()).thenReturn(subscriptionId);

        when(message.getProjectId()).thenReturn(projectId);
        when(message.getWorkspaceId()).thenReturn(workspaceId);
    }

    @Test
    @DisplayName("It should filter out the same client producer")
    void accept_filterOutSameClientProducer() {
        when(projectCreatedRTMConsumable.getRTMClientContext()).thenReturn(rtmClientContext);

        createdRTMConsumer.accept(rtmClient, projectCreatedRTMConsumable);

        verify(session.getRemote(), never()).sendStringByFuture(anyString());
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket")
    void accept_writesToWebSocket() {
        RTMClientContext producer = mock(RTMClientContext.class);
        when(producer.getClientId()).thenReturn("differentClientId");
        when(projectCreatedRTMConsumable.getRTMClientContext()).thenReturn(producer);

        createdRTMConsumer.accept(rtmClient, projectCreatedRTMConsumable);

        final String expected = "{" +
                "\"type\":\"workspace.broadcast\"," +
                "\"response\":{" +
                    "\"rtmEvent\":\"PROJECT_CREATED\"," +
                    "\"action\":\"CREATED\"," +
                    "\"projectId\":\"" + projectId + "\"," +
                    "\"workspaceId\":\"" + workspaceId + "\"" +
                "}," +
                "\"replyTo\":\"" + subscriptionId + "\"}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }
}