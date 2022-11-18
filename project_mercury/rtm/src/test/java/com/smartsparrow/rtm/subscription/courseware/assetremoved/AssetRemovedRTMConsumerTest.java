package com.smartsparrow.rtm.subscription.courseware.assetremoved;

import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
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
import com.smartsparrow.rtm.subscription.courseware.message.AssetRemovedBroadcastMessage;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.UUIDs;

class AssetRemovedRTMConsumerTest {

    @InjectMocks
    private AssetRemovedRTMConsumer assetRemovedRTMConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private AssetRemovedRTMConsumable assetRemovedRTMConsumable;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private AssetRemovedBroadcastMessage message;

    private static final String broadcastType = "author.activity.broadcast";
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final UUID interactiveId = UUIDs.timeBased();
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClient.getRtmClientContext()).thenReturn(rtmClientContext);
        when(rtmClientContext.getClientId()).thenReturn("clientId");
        when(rtmClient.getSession()).thenReturn(session);

        when(assetRemovedRTMConsumable.getContent()).thenReturn(message);
        when(assetRemovedRTMConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(assetRemovedRTMConsumable.getSubscriptionId()).thenReturn(subscriptionId);

        when(message.getElementId()).thenReturn(interactiveId);
        when(message.getElementType()).thenReturn(INTERACTIVE);
    }

    @Test
    @DisplayName("It should filter out the same client producer")
    void accept_filterOutSameClientProducer() {
        when(assetRemovedRTMConsumable.getRTMClientContext()).thenReturn(rtmClientContext);

        assetRemovedRTMConsumer.accept(rtmClient, assetRemovedRTMConsumable);

        verify(session.getRemote(), never()).sendStringByFuture(anyString());
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket")
    void accept_writesToWebSocket() {
        RTMClientContext producer = mock(RTMClientContext.class);
        when(producer.getClientId()).thenReturn("differentClientId");
        when(assetRemovedRTMConsumable.getRTMClientContext()).thenReturn(producer);

        assetRemovedRTMConsumer.accept(rtmClient, assetRemovedRTMConsumable);

        final String expected = "{" +
                "\"type\":\"activity.broadcast\"," +
                "\"response\":{" +
                "\"elementId\":\"" + interactiveId + "\"," +
                "\"elementType\":\"" + INTERACTIVE + "\"," +
                "\"rtmEvent\":\"INTERACTIVE_ASSET_REMOVED\"," +
                "\"action\":\"ASSET_REMOVED\"" +
                "}," +
                "\"replyTo\":\"" + subscriptionId + "\"}";

        assertNotNull(session.getRemote().sendStringByFuture(expected));
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

}
