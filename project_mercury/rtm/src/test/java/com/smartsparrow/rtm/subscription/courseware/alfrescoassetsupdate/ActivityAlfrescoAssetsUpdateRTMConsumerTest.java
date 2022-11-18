package com.smartsparrow.rtm.subscription.courseware.alfrescoassetsupdate;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.workspace.service.AlfrescoAssetSyncType.PUSH;
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

import com.smartsparrow.pubsub.subscriptions.alfrescoassetsupdate.ActivityAlfrescoAssetsUpdateRTMConsumable;
import com.smartsparrow.pubsub.subscriptions.alfrescoassetsupdate.AlfrescoAssetsUpdateBroadcastMessage;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.workspace.service.AlfrescoAssetSyncType;

class ActivityAlfrescoAssetsUpdateRTMConsumerTest {

    @InjectMocks
    private ActivityAlfrescoAssetsUpdateRTMConsumer updatedRTMConsumer;

    @Mock
    private RTMClient rtmClient;

    @Mock
    private ActivityAlfrescoAssetsUpdateRTMConsumable activityAlfrescoAssetsUpdateRTMConsumable;

    @Mock
    private RTMClientContext rtmClientContext;

    @Mock
    private AlfrescoAssetsUpdateBroadcastMessage message;

    private static final String broadcastType = "author.activity.broadcast";
    private static final UUID subscriptionId = UUIDs.timeBased();
    private static final UUID activityId = UUIDs.timeBased();
    private static final UUID assetId = UUIDs.timeBased();
    private static final AlfrescoAssetSyncType alfrescoSyncType = PUSH;
    private static final boolean isAlfrescoAssetUpdated = true;
    private static final boolean isAlfrescoSyncComplete = false;
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(rtmClient.getRtmClientContext()).thenReturn(rtmClientContext);
        when(rtmClientContext.getClientId()).thenReturn("clientId");
        when(rtmClient.getSession()).thenReturn(session);

        when(activityAlfrescoAssetsUpdateRTMConsumable.getContent()).thenReturn(message);
        when(activityAlfrescoAssetsUpdateRTMConsumable.getBroadcastType()).thenReturn(broadcastType);
        when(activityAlfrescoAssetsUpdateRTMConsumable.getSubscriptionId()).thenReturn(subscriptionId);

        when(message.getElementId()).thenReturn(activityId);
        when(message.getElementType()).thenReturn(ACTIVITY);
        when(message.getAssetId()).thenReturn(assetId);
        when(message.getAlfrescoSyncType()).thenReturn(alfrescoSyncType);
        when(message.isAlfrescoAssetUpdated()).thenReturn(isAlfrescoAssetUpdated);
        when(message.isAlfrescoSyncComplete()).thenReturn(isAlfrescoSyncComplete);
    }

    @Test
    @DisplayName("It should not filter out the same client producer")
    void accept_filterOutSameClientProducer() {
        updatedRTMConsumer.accept(rtmClient, activityAlfrescoAssetsUpdateRTMConsumable);

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(anyString());
    }

    @Test
    @DisplayName("It should write the broadcast message to the websocket")
    void accept_writesToWebSocket() {
        RTMClientContext producer = mock(RTMClientContext.class);
        when(producer.getClientId()).thenReturn("differentClientId");

        updatedRTMConsumer.accept(rtmClient, activityAlfrescoAssetsUpdateRTMConsumable);

        final String expected = "{" +
                "\"type\":\"activity.broadcast\"," +
                "\"response\":{" +
                "\"elementId\":\"" + activityId + "\"," +
                "\"elementType\":\"" + ACTIVITY + "\"," +
                "\"assetId\":\"" + assetId + "\"," +
                "\"alfrescoSyncType\":\"" + alfrescoSyncType + "\"," +
                "\"isAlfrescoAssetUpdated\":\"" + isAlfrescoAssetUpdated + "\"," +
                "\"isAlfrescoSyncComplete\":\"" + isAlfrescoSyncComplete + "\"," +
                "\"rtmEvent\":\"ACTIVITY_ALFRESCO_ASSETS_UPDATE\"," +
                "\"action\":\"ALFRESCO_ASSETS_UPDATE\"" +
                "}," +
                "\"replyTo\":\"" + subscriptionId + "\"}";

        assertNotNull(session.getRemote().sendStringByFuture(expected));
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

}
