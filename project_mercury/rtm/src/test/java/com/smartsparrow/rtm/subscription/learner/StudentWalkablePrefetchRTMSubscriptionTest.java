package com.smartsparrow.rtm.subscription.learner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.util.UUIDs;

class StudentWalkablePrefetchRTMSubscriptionTest {

    @InjectMocks
    private StudentWalkablePrefetchRTMSubscription subscription;

    @Mock
    private StudentWalkablePrefetchRTMSubscription.StudentWalkablePrefetchRTMSubscriptionFactory studentWalkablePrefetchRTMSubscriptionFactory;

    private static final UUID studentId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        subscription = new StudentWalkablePrefetchRTMSubscription(studentId);
        when(studentWalkablePrefetchRTMSubscriptionFactory.create(studentId)).thenReturn(subscription);
    }

    @Test
    void create() {
        assertEquals(studentWalkablePrefetchRTMSubscriptionFactory.create(studentId), subscription);
        assertEquals(StudentWalkablePrefetchRTMSubscription.NAME(studentId), subscription.getName());
        assertNotNull(subscription.getId());
        assertEquals("learner.student.walkable.prefetch.broadcast", subscription.getBroadcastType());
        assertEquals(StudentWalkablePrefetchRTMSubscription.class, subscription.getSubscriptionType());
    }

}
