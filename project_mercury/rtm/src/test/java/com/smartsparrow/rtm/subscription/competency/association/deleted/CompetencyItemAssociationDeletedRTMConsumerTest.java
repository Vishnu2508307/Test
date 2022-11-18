package com.smartsparrow.rtm.subscription.competency.association.deleted;

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

import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.UUIDs;

public class CompetencyItemAssociationDeletedRTMConsumerTest {

    @InjectMocks
    private CompetencyItemAssociationDeletedRTMConsumer associationDeletedRTMConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private CompetencyItemAssociationDeletedRTMConsumable associationDeletedRTMConsumable;

    @Mock
    private CompetencyDocumentBroadcastMessage message;

    private static final String broadcastType = "workspace.competency.document.broadcast";
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final UUID associationId = UUIDs.timeBased();
    private static final UUID documentId = UUIDs.timeBased();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClient.getSession()).thenReturn(session);

        when(associationDeletedRTMConsumable.getContent()).thenReturn(message);
        when(associationDeletedRTMConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(associationDeletedRTMConsumable.getSubscriptionId()).thenReturn(subscriptionId);

        when(message.getAssociationId()).thenReturn(associationId);
        when(message.getDocumentId()).thenReturn(documentId);
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket")
    void accept_writesToWebSocket() {

        associationDeletedRTMConsumer.accept(rtmClient, associationDeletedRTMConsumable);

        final String expected = "{" +
                "\"type\":\"workspace.competency.document.broadcast\"," +
                "\"response\":{" +
                "\"rtmEvent\":\"COMPETENCY_ASSOCIATION_DELETED\"," +
                "\"data\":{" +
                "\"documentId\":\"" + documentId + "\"," +
                "\"id\":\"" + associationId + "\"" +
                "}," +
                "\"action\":\"ASSOCIATION_DELETED\"" +
                "}," +
                "\"replyTo\":\"" + subscriptionId + "\"}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }
}
