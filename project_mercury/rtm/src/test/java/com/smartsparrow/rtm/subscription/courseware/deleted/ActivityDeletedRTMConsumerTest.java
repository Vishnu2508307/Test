package com.smartsparrow.rtm.subscription.courseware.deleted;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
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
import com.smartsparrow.rtm.subscription.courseware.message.ActivityCreatedBroadcastMessage;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.UUIDs;

class ActivityDeletedRTMConsumerTest {

    @InjectMocks
    private ActivityDeletedRTMConsumer deletedRTMConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private ActivityDeletedRTMConsumable activityDeletedRTMConsumable;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private ActivityCreatedBroadcastMessage message;

    private static final String broadcastType = "author.activity.broadcast";
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final UUID activityId = UUIDs.timeBased();
    private static final UUID parentPathwayId = UUIDs.timeBased();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClient.getRtmClientContext()).thenReturn(rtmClientContext);
        when(rtmClientContext.getClientId()).thenReturn("clientId");
        when(rtmClient.getSession()).thenReturn(session);

        when(activityDeletedRTMConsumable.getContent()).thenReturn(message);
        when(activityDeletedRTMConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(activityDeletedRTMConsumable.getSubscriptionId()).thenReturn(subscriptionId);

        when(message.getElementId()).thenReturn(activityId);
        when(message.getElementType()).thenReturn(ACTIVITY);
        when(message.getParentPathwayId()).thenReturn(parentPathwayId);
    }

    @Test
    @DisplayName("It should filter out the same client producer")
    void accept_filterOutSameClientProducer() {
        when(activityDeletedRTMConsumable.getRTMClientContext()).thenReturn(rtmClientContext);

        deletedRTMConsumer.accept(rtmClient, activityDeletedRTMConsumable);

        verify(session.getRemote(), never()).sendStringByFuture(anyString());
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket")
    void accept_writesToWebSocket() {
        RTMClientContext producer = mock(RTMClientContext.class);
        when(producer.getClientId()).thenReturn("differentClientId");
        when(activityDeletedRTMConsumable.getRTMClientContext()).thenReturn(producer);

        deletedRTMConsumer.accept(rtmClient, activityDeletedRTMConsumable);

        final String expected = "{" +
                "\"type\":\"activity.broadcast\"," +
                "\"response\":{" +
                    "\"elementId\":\"" + activityId + "\"," +
                    "\"elementType\":\"" + ACTIVITY + "\"," +
                    "\"parentPathwayId\":\"" + parentPathwayId + "\"," +
                    "\"rtmEvent\":\"ACTIVITY_DELETED\"," +
                    "\"action\":\"DELETED\"" +
                "}," +
                "\"replyTo\":\"" + subscriptionId + "\"}";

        assertNotNull(session.getRemote().sendStringByFuture(expected));
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

}
