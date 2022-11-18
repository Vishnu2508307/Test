package com.smartsparrow.pubsub.subscriptions.studentprogress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.util.UUIDs;

class StudentProgressRTMConsumableTest {

    private static final UUID studentId = UUIDs.timeBased();
    private static final UUID deploymentId = UUIDs.timeBased();
    private static final UUID elementId = UUIDs.timeBased();
    @Mock
    private Object progress;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void studentProgress() {

        StudentProgressBroadcastMessage message = new StudentProgressBroadcastMessage().
                setStudentId(studentId)
                .setDeploymentId(deploymentId)
                .setCoursewareElementId(elementId)
                .setProgress(progress);
        StudentProgressRTMConsumable consumable = new StudentProgressRTMConsumable(message);

        assertEquals(new StudentProgressRTMEvent().getName(), consumable.getRTMEvent().getName());
        assertEquals(String.format("learner.progress/%s/%s/%s", studentId, deploymentId, elementId),
                     consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }

}
