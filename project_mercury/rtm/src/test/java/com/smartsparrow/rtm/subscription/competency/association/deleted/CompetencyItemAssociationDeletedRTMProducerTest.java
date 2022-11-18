package com.smartsparrow.rtm.subscription.competency.association.deleted;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.util.UUIDs;

public class CompetencyItemAssociationDeletedRTMProducerTest {

    @InjectMocks
    private CompetencyItemAssociationDeletedRTMProducer associationDeletedRTMProducer;

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
        associationDeletedRTMProducer.buildAssociationDeletedRTMConsumable(rtmClientContext, associationId, documentId);
        assertNotNull(associationDeletedRTMProducer.getEventConsumable());
    }
}
