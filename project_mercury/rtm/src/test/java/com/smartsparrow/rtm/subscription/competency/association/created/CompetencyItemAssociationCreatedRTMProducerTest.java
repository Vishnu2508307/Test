package com.smartsparrow.rtm.subscription.competency.association.created;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.util.UUIDs;

public class CompetencyItemAssociationCreatedRTMProducerTest {

    @InjectMocks
    private CompetencyItemAssociationCreatedRTMProducer associationCreatedRTMProducer;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID associationId = UUIDs.timeBased();
    private static final UUID documentId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        associationCreatedRTMProducer.buildAssociationCreatedRTMConsumable(rtmClientContext, associationId, documentId);
        assertNotNull(associationCreatedRTMProducer.getEventConsumable());
    }
}
