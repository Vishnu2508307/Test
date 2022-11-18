package com.smartsparrow.iam.service;


import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.iam.data.SubscriptionGateway;

import reactor.core.publisher.Flux;

public class SubscriptionServiceTest {

    @Mock
    private SubscriptionGateway subscriptionGateway;

    @InjectMocks
    private SubscriptionService subscriptionService;

    //
    private UUID subscriptionId;
    private String subscriptionName = "enterprise";
    private Region subscriptionRegion = Region.GLOBAL;
    private Subscription subscription;

    @BeforeEach
    public void setup() {
        // create any @Mock
        MockitoAnnotations.initMocks(this);

        //
        subscriptionId = UUIDs.timeBased();

        //
        subscription = new Subscription().setId(subscriptionId)
                .setName(subscriptionName)
                .setIamRegion(subscriptionRegion);
        when(subscriptionGateway.fetchSubscription(eq(subscriptionId))).thenReturn(Flux.just(subscription));
    }

    @Test
    public void create() throws Exception {
        Subscription actual = subscriptionService.create(subscriptionName, Region.GLOBAL);

        assertAll("subscription",
                () -> {
                    assertNotNull(actual);

                    assertAll(
                            () -> assertNotNull(actual.getId()),
                            () -> assertEquals(subscriptionName, actual.getName()),
                            () -> assertEquals(Region.GLOBAL, actual.getIamRegion())
                    );
                }
        );
    }

    @Test
    public void create_noRegion() throws Exception {
        assertThrows(NullPointerException.class, () -> subscriptionService.create("kaboom", null));
    }

    @Test
    public void find() throws Exception {
        Subscription first = subscriptionService.find(subscriptionId).blockFirst();

        assertAll("subscription",
                () -> {
                    assertNotNull(first);

                    assertAll(
                            () -> assertEquals(subscriptionId, first.getId()),
                            () -> assertEquals(subscriptionName, first.getName()),
                            () -> assertEquals(Region.GLOBAL, first.getIamRegion())
                    );
                }
        );
    }

    @Test
    public void find_no_results() throws Exception {
        when(subscriptionGateway.fetchSubscription(eq(subscriptionId))).thenReturn(Flux.empty());
        Boolean hasElements = subscriptionService.find(subscriptionId).hasElements().block();

        assertTrue(hasElements != null && !hasElements);
    }

    @Test
    public void setRegion() {
        subscriptionService.setRegion(subscription, Region.US);

        verify(subscriptionGateway, atLeastOnce()).persistBlocking(any(Subscription.class));
    }

    @Test
    public void setRegion_invalid_subscription() {
        assertThrows(IllegalArgumentException.class, () -> subscriptionService.setRegion(null, Region.US));
    }

    @Test
    public void setRegion_invalid_region() {
        assertThrows(IllegalArgumentException.class, () -> subscriptionService.setRegion(subscription, null));
    }


}
