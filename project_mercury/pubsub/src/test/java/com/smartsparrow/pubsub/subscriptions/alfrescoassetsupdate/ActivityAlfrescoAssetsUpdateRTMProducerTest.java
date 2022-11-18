package com.smartsparrow.pubsub.subscriptions.alfrescoassetsupdate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.util.UUIDs;

class ActivityAlfrescoAssetsUpdateRTMProducerTest {

    @InjectMocks
    private ActivityAlfrescoAssetsUpdateRTMProducer activityAlfrescoAssetsUpdateRTMProducer;

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
    void buildConsumable() {
        activityAlfrescoAssetsUpdateRTMProducer.buildActivityAlfrescoAssetsUpdateRTMConsumable(activityId,
                                                                                               elementId,
                                                                                               elementType,
                                                                                               assetId,
                                                                                               alfrescoSyncType,
                                                                                               isAlfrescoAssetUpdated,
                                                                                               isAlfrescoSyncComplete);
        assertNotNull(activityAlfrescoAssetsUpdateRTMProducer.getEventConsumable());
    }

}
