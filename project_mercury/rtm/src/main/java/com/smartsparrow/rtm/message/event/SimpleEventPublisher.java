package com.smartsparrow.rtm.message.event;

import javax.inject.Inject;

import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;

import com.smartsparrow.dataevent.BroadcastMessage;

/**
 * Simple event publisher provides the injection of {@link CamelReactiveStreamsService} which has access specifier
 * protected and is therefore available in subclasses.
 *
 * @param <T> an object that holds entity specific information to broadcast in an event message
 */
public abstract class SimpleEventPublisher<T extends BroadcastMessage>  implements EventPublisher<T> {

    @Inject
    private CamelReactiveStreamsService camel;

    /**
     * Provides accessibility to the injected {@link CamelReactiveStreamsService}
     * @return the injected camel service
     */
    public CamelReactiveStreamsService getCamel() {
        return camel;
    }
}
