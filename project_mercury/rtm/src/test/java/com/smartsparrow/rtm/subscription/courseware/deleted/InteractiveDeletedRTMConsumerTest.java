package com.smartsparrow.rtm.subscription.courseware.deleted;

import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.InteractiveCreatedBroadcastMessage;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.UUIDs;

class InteractiveDeletedRTMConsumerTest {

    @InjectMocks
    private InteractiveDeletedRTMConsumer deletedRTMConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private InteractiveDeletedRTMConsumable interactiveDeletedRTMConsumable;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private InteractiveCreatedBroadcastMessage message;

    private static final String broadcastType = "author.activity.broadcast";
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final UUID parentElementId = UUIDs.timeBased();
    private static final UUID interactiveId = UUIDs.timeBased();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClient.getRtmClientContext()).thenReturn(rtmClientContext);
        when(rtmClientContext.getClientId()).thenReturn("clientId");
        when(rtmClient.getSession()).thenReturn(session);

        when(interactiveDeletedRTMConsumable.getContent()).thenReturn(message);
        when(interactiveDeletedRTMConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(interactiveDeletedRTMConsumable.getSubscriptionId()).thenReturn(subscriptionId);

        when(message.getElementId()).thenReturn(interactiveId);
        when(message.getElementType()).thenReturn(INTERACTIVE);
        when(message.getParentPathwayId()).thenReturn(parentElementId);
    }

    @Test
    @DisplayName("It should filter out the same client producer")
    void accept_filterOutSameClientProducer() {
        when(interactiveDeletedRTMConsumable.getRTMClientContext()).thenReturn(rtmClientContext);

        deletedRTMConsumer.accept(rtmClient, interactiveDeletedRTMConsumable);

        verify(session.getRemote(), never()).sendStringByFuture(anyString());
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket")
    void accept_writesToWebSocket() throws ExecutionException, InterruptedException {
        RTMClientContext producer = mock(RTMClientContext.class);
        when(producer.getClientId()).thenReturn("differentClientId");
        when(interactiveDeletedRTMConsumable.getRTMClientContext()).thenReturn(producer);

        deletedRTMConsumer.accept(rtmClient, interactiveDeletedRTMConsumable);

        final String expected = "{" +
                "\"type\":\"activity.broadcast\"," +
                "\"response\":{" +
                    "\"elementId\":\"" + interactiveId + "\"," +
                    "\"elementType\":\"" + INTERACTIVE + "\"," +
                    "\"parentElementId\":\"" + parentElementId + "\"," +
                    "\"rtmEvent\":\"INTERACTIVE_DELETED\"," +
                    "\"action\":\"DELETED\"" +
                "}," +
                "\"replyTo\":\"" + subscriptionId + "\"}";

        assertNotNull(session.getRemote().sendStringByFuture(expected));
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

}
