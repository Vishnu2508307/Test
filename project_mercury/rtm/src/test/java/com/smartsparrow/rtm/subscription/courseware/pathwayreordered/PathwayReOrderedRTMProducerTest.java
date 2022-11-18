package com.smartsparrow.rtm.subscription.courseware.pathwayreordered;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.util.UUIDs;

class PathwayReOrderedRTMProducerTest {

    @InjectMocks
    private PathwayReOrderedRTMProducer pathwayReOrderedRTMProducer;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID pathwayId = UUIDs.timeBased();
    private static final UUID idOne = UUIDs.timeBased();
    private static final UUID idTwo = UUIDs.timeBased();
    private static final List<WalkableChild> walkables = Lists.newArrayList(new WalkableChild().setElementId(idOne),
                                                                            new WalkableChild().setElementId(idTwo));
    private static final UUID activityId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        pathwayReOrderedRTMProducer.buildPathwayReOrderedRTMConsumable(rtmClientContext,
                                                                       activityId,
                                                                       pathwayId,
                                                                       walkables);
        assertNotNull(pathwayReOrderedRTMProducer.getEventConsumable());
    }

}
