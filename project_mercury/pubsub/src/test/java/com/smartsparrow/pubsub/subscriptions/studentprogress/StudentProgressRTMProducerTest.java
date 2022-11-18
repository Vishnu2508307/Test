package com.smartsparrow.pubsub.subscriptions.studentprogress;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.util.UUIDs;

class StudentProgressRTMProducerTest {

    @InjectMocks
    private StudentProgressRTMProducer studentProgressRTMProducer;
    @Mock
    private Object progress;

    private static final UUID studentId = UUIDs.timeBased();
    private static final UUID deploymentId = UUIDs.timeBased();
    private static final UUID elementId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        studentProgressRTMProducer.buildStudentProgressRTMConsumable(studentId,
                                                                     elementId,
                                                                     deploymentId,
                                                                     progress);
        assertNotNull(studentProgressRTMProducer.getEventConsumable());
    }

}
