package com.smartsparrow.rtm.subscription.courseware.created;

import static com.smartsparrow.courseware.data.CoursewareElementType.FEEDBACK;
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
import com.smartsparrow.rtm.subscription.courseware.message.FeedbackCreatedBroadcastMessage;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.UUIDs;

class FeedbackCreatedRTMConsumerTest {

    @InjectMocks
    private FeedbackCreatedRTMConsumer createdRTMConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private FeedbackCreatedRTMConsumable feedbackCreatedRTMConsumable;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private FeedbackCreatedBroadcastMessage message;

    private static final String broadcastType = "author.activity.broadcast";
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final UUID feedbackId = UUIDs.timeBased();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClient.getRtmClientContext()).thenReturn(rtmClientContext);
        when(rtmClientContext.getClientId()).thenReturn("clientId");
        when(rtmClient.getSession()).thenReturn(session);

        when(feedbackCreatedRTMConsumable.getContent()).thenReturn(message);
        when(feedbackCreatedRTMConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(feedbackCreatedRTMConsumable.getSubscriptionId()).thenReturn(subscriptionId);

        when(message.getElementId()).thenReturn(feedbackId);
        when(message.getElementType()).thenReturn(FEEDBACK);
    }

    @Test
    @DisplayName("It should filter out the same client producer")
    void accept_filterOutSameClientProducer() {
        when(feedbackCreatedRTMConsumable.getRTMClientContext()).thenReturn(rtmClientContext);

        createdRTMConsumer.accept(rtmClient, feedbackCreatedRTMConsumable);

        verify(session.getRemote(), never()).sendStringByFuture(anyString());
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket")
    void accept_writesToWebSocket() {
        RTMClientContext producer = mock(RTMClientContext.class);
        when(producer.getClientId()).thenReturn("differentClientId");
        when(feedbackCreatedRTMConsumable.getRTMClientContext()).thenReturn(producer);

        createdRTMConsumer.accept(rtmClient, feedbackCreatedRTMConsumable);

        final String expected = "{" +
                "\"type\":\"activity.broadcast\"," +
                "\"response\":{" +
                    "\"elementId\":\"" + feedbackId + "\"," +
                    "\"elementType\":\"" + FEEDBACK + "\"," +
                    "\"rtmEvent\":\"FEEDBACK_CREATED\"," +
                    "\"action\":\"CREATED\"" +
                "}," +
                "\"replyTo\":\"" + subscriptionId + "\"}";

        assertNotNull(session.getRemote().sendStringByFuture(expected));
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

}
