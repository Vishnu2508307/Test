package com.smartsparrow.rtm.subscription.iam;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.util.UUIDs;

class IamAccountProvisionRTMProducerTest {

    @InjectMocks
    private IamAccountProvisionRTMProducer iamAccountProvisionRTMProducer;

    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID accountId = UUIDs.timeBased();
    private static final UUID accountSubscriptionId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void buildConsumable() {
        iamAccountProvisionRTMProducer.buildAccountProvisionedRTMConsumable(rtmClientContext,
                                                                            accountSubscriptionId,
                                                                            accountId);
        assertNotNull(iamAccountProvisionRTMProducer.getEventConsumable());
    }

}
