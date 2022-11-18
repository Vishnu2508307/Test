package com.smartsparrow.rtm.subscription.iam;

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

class IamAccountProvisionRTMSubscriptionTest {

    @InjectMocks
    private IamAccountProvisionRTMSubscription subscription;

    @Mock
    private IamAccountProvisionRTMSubscription.IamAccountProvisionRTMSubscriptionFactory iamAccountProvisionRTMSubscriptionFactory;

    private static final UUID subscriptionId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        subscription = new IamAccountProvisionRTMSubscription(subscriptionId);
        when(iamAccountProvisionRTMSubscriptionFactory.create(subscriptionId)).thenReturn(subscription);

    }

    @Test
    void create() {
        assertEquals(iamAccountProvisionRTMSubscriptionFactory.create(subscriptionId), subscription);
        assertEquals(IamAccountProvisionRTMSubscription.NAME(subscriptionId), subscription.getName());
        assertNotNull(subscription.getId());
        assertEquals("iam.account.provision.broadcast", subscription.getBroadcastType());
        assertEquals(IamAccountProvisionRTMSubscription.class, subscription.getSubscriptionType());
    }

}
