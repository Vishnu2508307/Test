package com.smartsparrow.rtm.subscription.competency.association.created;

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

import com.smartsparrow.competency.data.ItemAssociation;
import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;
import com.smartsparrow.competency.service.ItemAssociationService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Mono;

public class CompetencyItemAssociationCreatedRTMConsumerTest {

    @InjectMocks
    private CompetencyItemAssociationCreatedRTMConsumer associationCreatedRTMConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private CompetencyItemAssociationCreatedRTMConsumable associationCreatedRTMConsumable;

    @Mock
    private CompetencyDocumentBroadcastMessage message;

    @Mock
    private ItemAssociationService itemAssociationService;

    @Mock
    private ItemAssociation itemAssociation;

    private static final String broadcastType = "workspace.competency.document.broadcast";
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final UUID associationId = UUIDs.timeBased();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(rtmClient.getSession()).thenReturn(session);
        when(associationCreatedRTMConsumable.getContent()).thenReturn(message);
        when(associationCreatedRTMConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(associationCreatedRTMConsumable.getSubscriptionId()).thenReturn(subscriptionId);
        when(message.getAssociationId()).thenReturn(associationId);
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket")
    void accept_writesToWebSocket() {
        when(itemAssociationService.findById(associationId)).thenReturn(Mono.just(itemAssociation));

        associationCreatedRTMConsumer.accept(rtmClient, associationCreatedRTMConsumable);

        final String expected = "{" +
                "\"type\":\"workspace.competency.document.broadcast\"," +
                "\"response\":{" +
                "\"rtmEvent\":\"COMPETENCY_ASSOCIATION_CREATED\"," +
                "\"data\":{}," +
                "\"action\":\"ASSOCIATION_CREATED\"" +
                "}," +
                "\"replyTo\":\"" + subscriptionId + "\"}";

        verify(session.getRemote()).sendStringByFuture(expected);
    }
}
