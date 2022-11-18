package data;

/**
 * Represents something that can be exchanged as a {@link Message} content
 * during a Differential Synchronization
 */
public interface Exchangeable {

    /**
     * Describes the types of messages that can be exchanged during a DiffSync
     */
    public static enum Type {
        // acknowledges applied patch
        ACK,
        // contains edit to send to the other end of diff sync
        PATCH,
        // signals the initiation requests of a diff sync
        START,
        // signals the termination of a diff sync
        END
    }

    Type getType();

}
