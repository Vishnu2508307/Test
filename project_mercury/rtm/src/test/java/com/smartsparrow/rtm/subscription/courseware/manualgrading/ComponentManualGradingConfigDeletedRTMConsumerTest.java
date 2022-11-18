package com.smartsparrow.rtm.subscription.courseware.manualgrading;

import static com.smartsparrow.courseware.data.CoursewareElementType.COMPONENT;
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
import com.smartsparrow.rtm.subscription.courseware.message.ActivityBroadcastMessage;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.UUIDs;

class ComponentManualGradingConfigDeletedRTMConsumerTest {

    @InjectMocks
    private ComponentManualGradingConfigDeletedRTMConsumer rtmConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private ComponentManualGradingConfigDeletedRTMConsumable componentManualGradingConfigDeletedRTMConsumable;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private ActivityBroadcastMessage message;

    private static final String broadcastType = "author.activity.broadcast";
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final UUID componentId = UUIDs.timeBased();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClient.getRtmClientContext()).thenReturn(rtmClientContext);
        when(rtmClientContext.getClientId()).thenReturn("clientId");
        when(rtmClient.getSession()).thenReturn(session);

        when(componentManualGradingConfigDeletedRTMConsumable.getContent()).thenReturn(message);
        when(componentManualGradingConfigDeletedRTMConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(componentManualGradingConfigDeletedRTMConsumable.getSubscriptionId()).thenReturn(subscriptionId);

        when(message.getElementId()).thenReturn(componentId);
        when(message.getElementType()).thenReturn(COMPONENT);
    }

    @Test
    @DisplayName("It should filter out the same client producer")
    void accept_filterOutSameClientProducer() {
        when(componentManualGradingConfigDeletedRTMConsumable.getRTMClientContext()).thenReturn(rtmClientContext);

        rtmConsumer.accept(rtmClient, componentManualGradingConfigDeletedRTMConsumable);

        verify(session.getRemote(), never()).sendStringByFuture(anyString());
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket")
    void accept_writesToWebSocket() {
        RTMClientContext producer = mock(RTMClientContext.class);
        when(producer.getClientId()).thenReturn("differentClientId");
        when(componentManualGradingConfigDeletedRTMConsumable.getRTMClientContext()).thenReturn(producer);

        rtmConsumer.accept(rtmClient, componentManualGradingConfigDeletedRTMConsumable);

        final String expected = "{" +
                "\"type\":\"activity.broadcast\"," +
                "\"response\":{" +
                    "\"elementId\":\"" + componentId + "\"," +
                    "\"elementType\":\"" + COMPONENT + "\"," +
                    "\"rtmEvent\":\"COMPONENT_MANUAL_GRADING_CONFIGURATION_DELETED\"," +
                    "\"action\":\"MANUAL_GRADING_CONFIGURATION_DELETED\"" +
                "}," +
                "\"replyTo\":\"" + subscriptionId + "\"}";

        assertNotNull(session.getRemote().sendStringByFuture(expected));
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

}
