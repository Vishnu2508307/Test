package com.smartsparrow.dataevent.eventmessage;

import java.io.Serializable;

public interface EventMessage<T> extends Serializable {

  /**
     * Channel Name defined by the constructor
     *
     * @return the channel name
     */
    String getName();

    /**
     * The RTMClientContext.clientId (or any other unique marker) of who is originating this event
     *
     * @return the clientId of the message creator
     */
    String getProducingClientId();

    /**
     * The body/content of the event.
     *
     */
    T getContent();

    /**
     * Builds a relevant channel name from for this event type.
     *
     * This is informational only! It will not modify the class instance.
     *
     * @return a formatted redis channel name relevant to supplied parameter.
     */
    String buildChannelName(String values);

}
