package com.smartsparrow.rtm.subscription.learner.studentprogress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.util.UUIDs;

class StudentProgressRTMSubscriptionTest {

    @InjectMocks
    private StudentProgressRTMSubscription subscription;

    private static final UUID studentId = UUIDs.timeBased();
    private static final UUID deploymentId = UUIDs.timeBased();
    private static final UUID elementId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        subscription = new StudentProgressRTMSubscription(deploymentId, elementId, studentId);
    }

    @Test
    void create() {
        assertEquals(StudentProgressRTMSubscription.NAME(studentId, deploymentId, elementId), subscription.getName());
        assertNotNull(subscription.getId());
        assertEquals("learner.progress.broadcast", subscription.getBroadcastType());
        assertEquals(StudentProgressRTMSubscription.class, subscription.getSubscriptionType());
    }

}
