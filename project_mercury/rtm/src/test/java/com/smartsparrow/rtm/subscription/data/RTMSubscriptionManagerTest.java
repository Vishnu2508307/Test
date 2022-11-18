package com.smartsparrow.rtm.subscription.data;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.rtm.subscription.SubscriptionAlreadyExists;
import com.smartsparrow.rtm.subscription.SubscriptionLimitExceeded;
import com.smartsparrow.rtm.subscription.SubscriptionNotFound;
import com.smartsparrow.rtm.ws.RTMClient;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


class RTMSubscriptionManagerTest {

    private static final String SUBSCRIPTION_NAME = "test";

    @Mock
    private RTMClient rtmClient;

    @InjectMocks
    private RTMSubscriptionManager manager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void add() {
        RTMSubscription subscription = createSubscriptionMock(SUBSCRIPTION_NAME);

        manager.add(subscription).block();

        verify(subscription, times(1)).subscribe(rtmClient);
    }

    @Test
    void add_nullSubscription() {
        NullPointerException e = assertThrows(NullPointerException.class, () -> manager.add(null));
        assertEquals("Subscription can not be null", e.getMessage());
    }

    @Test
    void add_nullChannelName() {
        RTMSubscription subscription = createSubscriptionMock(null);
        when(subscription.getName()).thenReturn(null);
        NullPointerException e = assertThrows(NullPointerException.class, () -> manager.add(subscription));
        assertEquals("channel name can not be null", e.getMessage());

    }

    @Test
    void add_twice() {

        RTMSubscription subscription = createSubscriptionMock(SUBSCRIPTION_NAME);

        // add once
        manager.add(subscription).block();

        //add same again
        StepVerifier.create(manager.add(subscription))
                .expectError(SubscriptionAlreadyExists.class)
                .verify();
    }

    @Test
    void add_limitExceeded() {
        //add 10 subscriptions
        for (int i = 0; i < 50; i++) {
            manager.add(createSubscription(String.format("%s_%d", SUBSCRIPTION_NAME, i)));
        }
        //add one more
        RTMSubscription subscription = createSubscription(SUBSCRIPTION_NAME);
        StepVerifier.create(manager.add(subscription))
                .expectError(SubscriptionLimitExceeded.class)
                .verify();
    }

    @Test
    void unsubscribe() {
        RTMSubscription subscription = createSubscriptionMock(SUBSCRIPTION_NAME);

        manager.unsubscribe(subscription);

        verify(subscription, times(1)).unsubscribe(rtmClient);
    }

    @Test
    void unsubscribe_afterSubscribe() throws Exception {
        RTMSubscription subscription = createSubscriptionMock(SUBSCRIPTION_NAME);
        manager.add(subscription);

        manager.unsubscribe(subscription.getName());

        verify(subscription, times(1)).unsubscribe(rtmClient);
    }

    @Test
    void unsubscribe_whenNotSubscribed() {
        assertThrows(SubscriptionNotFound.class, () -> manager.unsubscribe(SUBSCRIPTION_NAME));
    }

    @Test
    void unsubscribeAll() throws Exception {
        //add 10 subscriptions
        List<RTMSubscription> list = new ArrayList<>(3);
        RTMSubscription subscription;
        for (int i = 0; i < 3; i++) {
            subscription = createSubscriptionMock(String.format("%s_%d", SUBSCRIPTION_NAME, i));
            list.add(subscription);
            manager.add(subscription);
        }

        manager.unsubscribeAll();

        for (RTMSubscription s : list) {
            verify(s, times(1)).unsubscribe(rtmClient);
        }
    }

    private RTMSubscription createSubscription(String name) {
        return new RTMSubscription() {

            @Override
            public UUID getId() {
                return UUID.randomUUID();
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getBroadcastType() {
                return null;
            }

            @Override
            public Mono<Integer> subscribe(RTMClient rtmClient) {

                return Mono.empty();
            }

            @Override
            public void unsubscribe(RTMClient rtmClient) {

            }
        };
    }

    private RTMSubscription createSubscriptionMock(String name) {
        RTMSubscription subscription = mock(RTMSubscription.class);
        when(subscription.getName()).thenReturn(name);
        when(subscription.subscribe(any(RTMClient.class))).thenReturn(Mono.just(1));
        return subscription;
    }
}
