package com.smartsparrow.rtm.subscription.learner.studentscope;

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

public class StudentScopeRTMSubscriptionTest {

    @InjectMocks
    private StudentScopeRTMSubscription subscription;

    @Mock
    private StudentScopeRTMSubscription.StudentScopeRTMSubscriptionFactory studentScopeRTMSubscriptionFactory;

    private static final UUID studentId = UUIDs.timeBased();
    private static final UUID deploymentId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        subscription = new StudentScopeRTMSubscription(studentId,deploymentId);
        when(studentScopeRTMSubscriptionFactory.create(studentId,deploymentId)).thenReturn(subscription);
    }

    @Test
    void create() {
        assertEquals(studentScopeRTMSubscriptionFactory.create(studentId,deploymentId), subscription);
        assertEquals(StudentScopeRTMSubscription.NAME(studentId, deploymentId), subscription.getName());
        assertNotNull(subscription.getId());
        assertEquals("learner.student.scope.broadcast", subscription.getBroadcastType());
        assertEquals(StudentScopeRTMSubscription.class, subscription.getSubscriptionType());
    }
}
