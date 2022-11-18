package com.smartsparrow.rtm.subscription.competency;

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

public class CompetencyDocumentEventRTMSubscriptionTest {

    @InjectMocks
    private CompetencyDocumentEventRTMSubscription subscription;

    @Mock
    private CompetencyDocumentEventRTMSubscription.CompetencyDocumentEventRTMSubscriptionFactory competencyDocumentEventRTMSubscriptionFactory;

    private static final UUID documentId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        subscription = new CompetencyDocumentEventRTMSubscription(documentId);
        when(competencyDocumentEventRTMSubscriptionFactory.create(documentId)).thenReturn(subscription);
    }

    @Test
    void create() {
        assertEquals(CompetencyDocumentEventRTMSubscription.NAME(documentId), subscription.getName());
        assertNotNull(subscription.getId());
        assertEquals("workspace.competency.document.broadcast", subscription.getBroadcastType());
        assertEquals(CompetencyDocumentEventRTMSubscription.class, subscription.getSubscriptionType());
    }
}
