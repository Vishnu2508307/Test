package com.smartsparrow.pubsub.subscriptions.studentscope;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.util.UUIDs;

public class StudentScopeProducerTest {

    @InjectMocks
    private StudentScopeProducer studentScopeProducer;

    @Inject
    private Object studentScopeEntry;

    private static final UUID studentId = UUIDs.timeBased();
    private static final UUID deploymentId = UUIDs.timeBased();
    private static final UUID studentScopeUrn = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        studentScopeProducer.buildStudentScopeConsumable(studentId, deploymentId,
                                                            studentScopeUrn, studentScopeEntry);
        assertNotNull(studentScopeProducer.getEventConsumable());
    }

}
