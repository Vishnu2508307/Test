package com.smartsparrow.pubsub.subscriptions.learner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.util.UUIDs;

class StudentWalkablePrefetchConsumableTest {

    private static final UUID accountId = UUIDs.timeBased();
    @Mock
    private Object walkable;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create() {
        StudentWalkablePrefetchBroadcastMessage message = new StudentWalkablePrefetchBroadcastMessage(accountId, walkable);
        StudentWalkablePrefetchConsumable consumable = new StudentWalkablePrefetchConsumable(message);

        assertEquals(new StudentWalkablePrefetchRTMEvent().getName(), consumable.getRTMEvent().getName());
        assertEquals(String.format("student/%s", accountId), consumable.getSubscriptionName());
        assertNotNull(consumable.getName());
    }

}
