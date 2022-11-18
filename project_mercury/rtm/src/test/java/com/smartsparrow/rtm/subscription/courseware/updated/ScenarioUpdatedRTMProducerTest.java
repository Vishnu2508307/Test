package com.smartsparrow.rtm.subscription.courseware.updated;

import static com.smartsparrow.courseware.data.CoursewareElementType.SCENARIO;
import static com.smartsparrow.courseware.data.ScenarioLifecycle.ACTIVITY_COMPLETE;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.ScenarioLifecycle;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.util.UUIDs;

class ScenarioUpdatedRTMProducerTest {
    @InjectMocks
    private ScenarioUpdatedRTMProducer scenarioUpdatedRTMProducer;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID scenarioId = UUIDs.timeBased();
    private static final UUID parentElementId = UUIDs.timeBased();
    private static final CoursewareElementType parentElementType = SCENARIO;
    private static final ScenarioLifecycle lifecycle = ACTIVITY_COMPLETE;
    private static final UUID activityId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        scenarioUpdatedRTMProducer.buildScenarioUpdatedRTMConsumable(rtmClientContext, activityId, scenarioId, parentElementId, parentElementType, lifecycle);
        assertNotNull(scenarioUpdatedRTMProducer.getEventConsumable());
    }

}
