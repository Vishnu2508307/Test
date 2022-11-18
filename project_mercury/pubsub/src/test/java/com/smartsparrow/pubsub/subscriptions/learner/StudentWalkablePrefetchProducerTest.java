package com.smartsparrow.pubsub.subscriptions.learner;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.util.UUIDs;

class StudentWalkablePrefetchProducerTest {

    @InjectMocks
    private StudentWalkablePrefetchProducer studentWalkablePrefetchProducer;

    private static final UUID accountId = UUIDs.timeBased();
    @Mock
    private Object walkable;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        studentWalkablePrefetchProducer.buildStudentWalkablePrefetchConsumable( accountId, walkable);
        assertNotNull(studentWalkablePrefetchProducer.getEventConsumable());
    }

}
