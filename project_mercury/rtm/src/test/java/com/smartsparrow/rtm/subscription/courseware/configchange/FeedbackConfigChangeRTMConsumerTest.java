package com.smartsparrow.rtm.subscription.courseware.configchange;

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
import com.smartsparrow.rtm.subscription.courseware.message.ConfigChangeBroadcastMessage;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.UUIDs;

class FeedbackConfigChangeRTMConsumerTest {

    @InjectMocks
    private FeedbackConfigChangeRTMConsumer configChangeRTMConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private FeedbackConfigChangeRTMConsumable feedbackConfigChangeRTMConsumable;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private ConfigChangeBroadcastMessage message;

    private static final String broadcastType = "author.activity.broadcast";
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final UUID feedbackId = UUIDs.timeBased();
    private static final String config = "[{'foo': 'bar'}]";
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClient.getRtmClientContext()).thenReturn(rtmClientContext);
        when(rtmClientContext.getClientId()).thenReturn("clientId");
        when(rtmClient.getSession()).thenReturn(session);

        when(feedbackConfigChangeRTMConsumable.getContent()).thenReturn(message);
        when(feedbackConfigChangeRTMConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(feedbackConfigChangeRTMConsumable.getSubscriptionId()).thenReturn(subscriptionId);

        when(message.getElementId()).thenReturn(feedbackId);
        when(message.getElementType()).thenReturn(FEEDBACK);
        when(message.getConfig()).thenReturn(config);
    }

    @Test
    @DisplayName("It should filter out the same client producer")
    void accept_filterOutSameClientProducer() {
        when(feedbackConfigChangeRTMConsumable.getRTMClientContext()).thenReturn(rtmClientContext);

        configChangeRTMConsumer.accept(rtmClient, feedbackConfigChangeRTMConsumable);

        verify(session.getRemote(), never()).sendStringByFuture(anyString());
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket")
    void accept_writesToWebSocket() {
        RTMClientContext producer = mock(RTMClientContext.class);
        when(producer.getClientId()).thenReturn("differentClientId");
        when(feedbackConfigChangeRTMConsumable.getRTMClientContext()).thenReturn(producer);

        configChangeRTMConsumer.accept(rtmClient, feedbackConfigChangeRTMConsumable);

        final String expected = "{" +
                "\"type\":\"activity.broadcast\"," +
                "\"response\":{" +
                    "\"elementId\":\"" + feedbackId + "\"," +
                    "\"elementType\":\"" + FEEDBACK + "\"," +
                    "\"config\":\"" + config + "\"," +
                    "\"rtmEvent\":\"FEEDBACK_CONFIG_CHANGE\"," +
                    "\"action\":\"CONFIG_CHANGE\"" +
                "}," +
                "\"replyTo\":\"" + subscriptionId + "\"}";

        assertNotNull(session.getRemote().sendStringByFuture(expected));
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

}
