package com.smartsparrow.rtm.subscription.courseware.pathwayreordered;

import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.courseware.message.PathwayReOrderedBroadcastMessage;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.UUIDs;

class PathwayReOrderedRTMConsumerTest {

    @InjectMocks
    private PathwayReOrderedRTMConsumer reOrderedRTMConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private PathwayReOrderedRTMConsumable pathwayReOrderedRTMConsumable;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private PathwayReOrderedBroadcastMessage message;

    private static final String broadcastType = "author.activity.broadcast";
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final UUID pathwayId = UUIDs.timeBased();
    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private static final UUID idOne = UUIDs.timeBased();
    private static final UUID idTwo = UUIDs.timeBased();
    private static final List<UUID> walkableIds = Lists.newArrayList(idOne, idTwo);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClient.getRtmClientContext()).thenReturn(rtmClientContext);
        when(rtmClientContext.getClientId()).thenReturn("clientId");
        when(rtmClient.getSession()).thenReturn(session);

        when(pathwayReOrderedRTMConsumable.getContent()).thenReturn(message);
        when(pathwayReOrderedRTMConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(pathwayReOrderedRTMConsumable.getSubscriptionId()).thenReturn(subscriptionId);

        when(message.getElementId()).thenReturn(pathwayId);
        when(message.getElementType()).thenReturn(PATHWAY);
    }

    @Test
    @DisplayName("It should filter out the same client producer")
    void accept_filterOutSameClientProducer() {
        when(pathwayReOrderedRTMConsumable.getRTMClientContext()).thenReturn(rtmClientContext);

        reOrderedRTMConsumer.accept(rtmClient, pathwayReOrderedRTMConsumable);

        verify(session.getRemote(), never()).sendStringByFuture(anyString());
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket")
    void accept_writesToWebSocket() {
        RTMClientContext producer = mock(RTMClientContext.class);
        when(producer.getClientId()).thenReturn("differentClientId");
        when(pathwayReOrderedRTMConsumable.getRTMClientContext()).thenReturn(producer);

        reOrderedRTMConsumer.accept(rtmClient, pathwayReOrderedRTMConsumable);

        final String expected = "{" +
                "\"type\":\"activity.broadcast\"," +
                "\"response\":{" +
                "\"elementId\":\"" + pathwayId + "\"," +
                "\"elementType\":\"" + PATHWAY + "\"," +
                "\"walkables\":\"" + walkableIds + "\"," +
                "\"rtmEvent\":\"PATHWAY_REORDERED\"," +
                "\"action\":\"PATHWAY_REORDERED\"" +
                "}," +
                "\"replyTo\":\"" + subscriptionId + "\"}";

        assertNotNull(session.getRemote().sendStringByFuture(expected));
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}