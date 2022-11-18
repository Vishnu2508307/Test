package com.smartsparrow.rtm.subscription.competency.deleted;

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

public class DocumentItemDeletedRTMConsumerTest {

    @InjectMocks
    private DocumentItemDeletedRTMConsumer documentItemDeletedRTMConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private DocumentItemDeletedRTMConsumable documentItemDeletedRTMConsumable;

    @Mock
    private CompetencyDocumentBroadcastMessage message;

    private static final String broadcastType = "workspace.competency.document.broadcast";
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final UUID documentId = UUIDs.timeBased();
    private static final UUID documentItemId = UUIDs.timeBased();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClient.getSession()).thenReturn(session);

        when(documentItemDeletedRTMConsumable.getContent()).thenReturn(message);
        when(documentItemDeletedRTMConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(documentItemDeletedRTMConsumable.getSubscriptionId()).thenReturn(subscriptionId);

        when(message.getDocumentItemId()).thenReturn(documentItemId);
        when(message.getDocumentId()).thenReturn(documentId);
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket")
    void accept_writesToWebSocket() {

        documentItemDeletedRTMConsumer.accept(rtmClient, documentItemDeletedRTMConsumable);

        final String expected = "{" +
                "\"type\":\"workspace.competency.document.broadcast\"," +
                "\"response\":{" +
                "\"rtmEvent\":\"DOCUMENT_ITEM_DELETED\"," +
                "\"data\":{" +
                "\"documentId\":\"" + documentId + "\"," +
                "\"id\":\"" + documentItemId + "\"" +
                "}," +
                "\"action\":\"DOCUMENT_ITEM_DELETED\"" +
                "}," +
                "\"replyTo\":\"" + subscriptionId + "\"}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }
}
