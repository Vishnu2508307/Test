package com.smartsparrow.rtm.subscription.competency.created;

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
import com.smartsparrow.competency.payload.DocumentItemPayload;
import com.smartsparrow.competency.service.DocumentItemService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Mono;

public class DocumentItemCreatedRTMConsumerTest {

    @InjectMocks
    private DocumentItemCreatedRTMConsumer documentItemCreatedRTMConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private DocumentItemCreatedRTMConsumable documentItemCreatedRTMConsumable;

    @Mock
    private CompetencyDocumentBroadcastMessage message;

    @Mock
    private DocumentItemService documentItemService;

    private static final String broadcastType = "workspace.competency.document.broadcast";
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final UUID documentItemId = UUIDs.timeBased();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClient.getSession()).thenReturn(session);

        when(documentItemCreatedRTMConsumable.getContent()).thenReturn(message);
        when(documentItemCreatedRTMConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(documentItemCreatedRTMConsumable.getSubscriptionId()).thenReturn(subscriptionId);

        when(message.getDocumentItemId()).thenReturn(documentItemId);
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket")
    void accept_writesToWebSocket() {

        when(documentItemService.getDocumentItemPayload(message.getDocumentItemId())).thenReturn(Mono.just(new DocumentItemPayload()));

        documentItemCreatedRTMConsumer.accept(rtmClient, documentItemCreatedRTMConsumable);

        final String expected = "{" +
                "\"type\":\"workspace.competency.document.broadcast\"," +
                "\"response\":{" +
                "\"rtmEvent\":\"DOCUMENT_ITEM_CREATED\"," +
                "\"data\":{}," +
                "\"action\":\"DOCUMENT_ITEM_CREATED\"" +
                "}," +
                "\"replyTo\":\"" + subscriptionId + "\"}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }
}
