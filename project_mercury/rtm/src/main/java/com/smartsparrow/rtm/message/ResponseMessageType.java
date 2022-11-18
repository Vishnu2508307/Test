package com.smartsparrow.rtm.message;

/**
 * Interface to enforce a single required field in the websocket messages.
 *
 * It is used for polymorphic deserialization of the message.
 */
public interface ResponseMessageType extends MessageType {

    /**
     * Optional field, Return the id / replyTo tracking
     *
     * @return the string which identifies the message, null if not supplied
     */
    String getReplyTo();

}
