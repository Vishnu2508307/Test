package data;

import java.io.Serializable;

/**
 * Describes a Message flying on a channel during Differential Synchronization
 * @param <T> the body of the Message. The implementation will use either {@link Patch} or {@link Ack} as bodies
 */
public interface Message<T extends Exchangeable> extends Serializable {

    long serialVersionUID = 2997179687824223010L;

    /**
     *
     * @return the body of the message
     */
    T getBody();

    /**
     * Build a message object from body
     *
     * @param body the body of the message
     * @param <T> the body type
     * @return a new Message with a body
     */
    static <T extends Exchangeable> Message<T> build(T body) {
        return () -> body;
    }
}
