package data;

/**
 * Describes the Channel to keep open during Differential Synchronization
 * so messages can be exchanged back and forth to enable diff sync flow
 * and functionalities.
 */
public interface Channel {

    /**
     * Allows to send a message on a channel
     *
     * @param message the message to send over the channel
     */
    void send(Message<? extends Exchangeable> message);

    /**
     * Receives a message from a channel
     * @param message the incoming message on the channel
     */
    void receive(Message<? extends Exchangeable> message);
}
