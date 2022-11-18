package com.smartsparrow.rtm.message.event;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Provider;
import com.smartsparrow.dataevent.BroadcastMessage;
import com.smartsparrow.rtm.message.event.lang.EventPublisherNotFoundException;
import com.smartsparrow.rtm.wiring.RTMScope;
import com.smartsparrow.rtm.wiring.RTMScoped;
import com.smartsparrow.rtm.ws.RTMClient;

@RTMScoped
public class RTMEventBroker {

    private static final Logger log = LoggerFactory.getLogger(RTMEventBroker.class);

    private final Map<String, Collection<Provider<EventPublisher<? extends BroadcastMessage>>>> publishers;
    private final RTMClient rtmClient;

    /**
     * publishers and rtmClient are initialised in
     * {@link com.smartsparrow.rtm.ws.RTMWebSocketHandler#initialise(String, Session)} then the RTMEventBroker is
     * seeded by the {@link RTMScope}. This constructor is required by Guice so that RTMEventBroker can be injected
     * as a {@link Provider}
     */
    @Inject
    public RTMEventBroker() {
        rtmClient = null;
        publishers = new HashMap<>();
    }

    public RTMEventBroker(Map<String, Collection<Provider<EventPublisher<? extends BroadcastMessage>>>> publishers,
                          RTMClient rtmClient) {
        this.publishers = publishers;
        this.rtmClient = rtmClient;
    }

    /**
     * Get a collection of {@link EventPublisher} for the supplied {@param messageType} and publishes the
     * {@param D} data for each event publisher.
     *
     * @param messageType the message type to find the collection of event publishers for
     * @param broadcastMessage the data to broadcast. Has to be of the same type as the first parameterized type of the implemented
     *             {@link EventPublisher}
     * @param <D> a generic object that holds data to broadcast
     */
    @SuppressWarnings("unchecked")
    public <D extends BroadcastMessage> void broadcast(String messageType, D broadcastMessage) {
        try {
            for (Provider<EventPublisher<? extends BroadcastMessage>> eventPublisherProvider : getPublishers(messageType)) {

                EventPublisher<BroadcastMessage> publisher = (EventPublisher<BroadcastMessage>) eventPublisherProvider.get();

                publisher.publish(rtmClient, broadcastMessage);
            }
        } catch (EventPublisherNotFoundException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while broadcasting", e);
            }
            throw e;
        }
    }

    /**
     * Attempts to find a collection of {@link EventPublisher} for the supplied {@param type}
     * @param type the type to find the event publishers for
     * @return a collection of EventPublisher
     * @throws EventPublisherNotFoundException when the collection is not found
     */
    private Collection<Provider<EventPublisher<? extends BroadcastMessage>>> getPublishers(String type) {
        Collection<Provider<EventPublisher<? extends BroadcastMessage>>> all = publishers.get(type);

        if (all == null) {
            throw new EventPublisherNotFoundException(type);
        }

        return all;
    }
}
