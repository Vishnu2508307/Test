package com.smartsparrow.rtm.message.event;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Map;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.dataevent.BroadcastMessage;
import com.smartsparrow.rtm.message.event.lang.EventPublisherNotFoundException;
import com.smartsparrow.rtm.ws.RTMClient;

class RTMEventBrokerTest {

    private RTMEventBroker rtmEventBroker;

    @Mock
    private Map<String, Collection<Provider<EventPublisher<? extends BroadcastMessage>>>> publishers;

    @Mock
    private RTMClient rtmClient;

    private static final String type = "type";
    private BroadcastMessage broadcastMessage;
    private String result;
    private Collection<Provider<EventPublisher<? extends BroadcastMessage>>> all;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        rtmEventBroker = new RTMEventBroker(publishers, rtmClient);
        result = null;

        broadcastMessage = mock(BroadcastMessage.class);

        all = Lists.newArrayList((Provider) () -> (EventPublisher<BroadcastMessage>) (rtmClient, message) -> {
            result = "ok";
        });
    }

    @Test
    void broadcast_publishersNotFoundForType() {
        EventPublisherNotFoundException e = assertThrows(EventPublisherNotFoundException.class,
                () -> rtmEventBroker.broadcast(type, broadcastMessage));
        assertNull(result);
        assertTrue(e.getMessage().contains(type));
    }

    @Test
    void broadcast_success() {
        assertNull(result);
        when(publishers.get(type)).thenReturn(all);
        rtmEventBroker.broadcast(type, broadcastMessage);
        assertNotNull(result);
    }
}
