package com.smartsparrow.rtm.message;

/**
 * Interface to enforce a single required field in the websocket messages.
 *
 * It is used for polymorphic deserialization of the message.
 */
public interface MessageType {

    /**
     * The type of this message.
     *
     * @return the string which identifies the message type
     */
    String getType();

}
