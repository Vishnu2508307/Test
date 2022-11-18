package com.smartsparrow.rtm.subscription.courseware.themechange;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.util.UUIDs;

class ActivityThemeChangeRTMProducerTest {

    @InjectMocks
    private ActivityThemeChangeRTMProducer activityThemeChangeRTMProducer;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID activityId = UUIDs.timeBased();
    private static final UUID rootElementId = UUIDs.timeBased();
    private static final String config = "theme config";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        activityThemeChangeRTMProducer.buildActivityThemeChangeRTMConsumable(rtmClientContext, rootElementId, activityId, config);
        assertNotNull(activityThemeChangeRTMProducer.getEventConsumable());
    }

}
