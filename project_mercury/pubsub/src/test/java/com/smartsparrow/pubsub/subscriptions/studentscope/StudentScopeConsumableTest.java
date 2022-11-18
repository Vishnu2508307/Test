package com.smartsparrow.pubsub.subscriptions.studentscope;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.util.UUIDs;

public class StudentScopeConsumableTest {

    @Mock
    private Object studentScopeEntry;

    private static final UUID studentId = UUIDs.timeBased();
    private static final UUID deploymentId = UUIDs.timeBased();
    private static final UUID studentScopeUrn = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void studentScope() {
        StudentScopeBroadcastMessage message = new StudentScopeBroadcastMessage().setStudentId(studentId)
                .setDeploymentId(deploymentId)
                .setStudentScopeUrn(studentScopeUrn)
                .setStudentScopeEntry(studentScopeEntry);
        StudentScopeConsumable consumable = new StudentScopeConsumable(message);

        assertEquals(new StudentScopeRTMEvent().getName(), consumable.getRTMEvent().getName());
        assertEquals(String.format("learner.student.scope/%s/%s", studentId, deploymentId),
                            consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }
}
