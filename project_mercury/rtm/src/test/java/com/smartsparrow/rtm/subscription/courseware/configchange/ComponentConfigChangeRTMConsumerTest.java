package com.smartsparrow.rtm.subscription.courseware.configchange;

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
import com.smartsparrow.rtm.subscription.courseware.message.ConfigChangeBroadcastMessage;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.UUIDs;

class ComponentConfigChangeRTMConsumerTest {

    @InjectMocks
    private ComponentConfigChangeRTMConsumer configChangeRTMConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private ComponentConfigChangeRTMConsumable componentConfigChangeRTMConsumable;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private ConfigChangeBroadcastMessage message;

    private static final String broadcastType = "author.activity.broadcast";
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final UUID componentId = UUIDs.timeBased();
    private static final String config = "[{'foo': 'bar'}]";
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClient.getRtmClientContext()).thenReturn(rtmClientContext);
        when(rtmClientContext.getClientId()).thenReturn("clientId");
        when(rtmClient.getSession()).thenReturn(session);

        when(componentConfigChangeRTMConsumable.getContent()).thenReturn(message);
        when(componentConfigChangeRTMConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(componentConfigChangeRTMConsumable.getSubscriptionId()).thenReturn(subscriptionId);

        when(message.getElementId()).thenReturn(componentId);
        when(message.getElementType()).thenReturn(COMPONENT);
        when(message.getConfig()).thenReturn(config);
    }

    @Test
    @DisplayName("It should filter out the same client producer")
    void accept_filterOutSameClientProducer() {
        when(componentConfigChangeRTMConsumable.getRTMClientContext()).thenReturn(rtmClientContext);

        configChangeRTMConsumer.accept(rtmClient, componentConfigChangeRTMConsumable);

        verify(session.getRemote(), never()).sendStringByFuture(anyString());
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket")
    void accept_writesToWebSocket() {
        RTMClientContext producer = mock(RTMClientContext.class);
        when(producer.getClientId()).thenReturn("differentClientId");
        when(componentConfigChangeRTMConsumable.getRTMClientContext()).thenReturn(producer);

        configChangeRTMConsumer.accept(rtmClient, componentConfigChangeRTMConsumable);

        final String expected = "{" +
                "\"type\":\"activity.broadcast\"," +
                "\"response\":{" +
                    "\"elementId\":\"" + componentId + "\"," +
                    "\"elementType\":\"" + COMPONENT + "\"," +
                    "\"config\":\"" + config + "\"," +
                    "\"rtmEvent\":\"COMPONENT_CONFIG_CHANGE\"," +
                    "\"action\":\"CONFIG_CHANGE\"" +
                "}," +
                "\"replyTo\":\"" + subscriptionId + "\"}";

        assertNotNull(session.getRemote().sendStringByFuture(expected));
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

}
