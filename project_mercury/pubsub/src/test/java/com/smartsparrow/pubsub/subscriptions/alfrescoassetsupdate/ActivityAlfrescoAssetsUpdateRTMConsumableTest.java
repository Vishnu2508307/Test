package com.smartsparrow.pubsub.subscriptions.alfrescoassetsupdate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.util.UUIDs;

class ActivityAlfrescoAssetsUpdateRTMConsumableTest {

    private static final UUID activityId = UUIDs.timeBased();
    private static final UUID elementId = UUIDs.timeBased();
    private static final UUID assetId = UUIDs.timeBased();
    private static final boolean isAlfrescoAssetUpdated = true;
    private static final boolean isAlfrescoSyncComplete = false;

    @Mock
    private Object alfrescoSyncType;
    @Mock
    private Object elementType;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create() {
        AlfrescoAssetsUpdateBroadcastMessage message = new AlfrescoAssetsUpdateBroadcastMessage(activityId,
                                                                                                elementId,
                                                                                                elementType,
                                                                                                assetId,
                                                                                                alfrescoSyncType,
                                                                                                isAlfrescoAssetUpdated,
                                                                                                isAlfrescoSyncComplete);
        ActivityAlfrescoAssetsUpdateRTMConsumable consumable = new ActivityAlfrescoAssetsUpdateRTMConsumable(message);

        assertEquals(new ActivityAlfrescoAssetsUpdateRTMEvent().getName(), consumable.getRTMEvent().getName());
        Assertions.assertEquals(String.format("author.activity/%s", activityId), consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }
}
