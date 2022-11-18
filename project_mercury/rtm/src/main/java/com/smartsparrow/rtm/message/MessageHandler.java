package com.smartsparrow.rtm.message;

import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.RTMWebSocketHandlerException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.subscription.SubscriptionAlreadyExists;
import com.smartsparrow.rtm.subscription.SubscriptionLimitExceeded;

import reactor.core.Exceptions;

/**
 * Interface that allows a class to process an incoming message of type T.
 *
 * @param <T> the type of message this processor will process
 */
public interface MessageHandler<T extends MessageType> {

    /**
     * Validate parameters on a message. The interface provides a default implementation that validates the message.
     * Should {@link Override} this method when custom validation over specific fields is required. The method should
     * throw an {@link RTMWebSocketHandlerException} with reason and error code when a required argument is not
     * supplied correctly
     * @param message the received message that requires to be validated
     * @throws RTMValidationException when the validation fails
     */
    default void validate(T message) throws RTMValidationException { }

    /**
     * Handle a new message.
     *
     * Responses must be written to {@code Session} the using the async methods!
     * {@see Responses.write()}
     * {@see session.getRemote().sendStringByFuture(json)}
     * {@see session.getRemote().sendString(json, WriteCallback)}
     *
     * @param session the websocket session
     * @param message the newly arrived message
     *
     */
    void handle(Session session, T message) throws WriteResponseException;

    /**
     * Convenience method with common pattern to deal with subscribe type message errors
     * @param exception
     */
    default void subscriptionOnErrorHandler(Throwable exception) {
        Throwable unwrap = Exceptions.unwrap(exception);
        if(unwrap instanceof SubscriptionLimitExceeded || unwrap instanceof SubscriptionAlreadyExists) {
            throw new IllegalArgumentFault(unwrap.getMessage());
        }
    }

}
