package com.smartsparrow.rtm.message;

/**
 * A base message which has been received on a WebSocket and deserialized to a registered type.
 *
 * The fields in this class and its subclasses are recommended to be immutable.
 */
abstract public class ReceivedMessage implements MessageType {

    private static final long WAIT_PENDING_TIMER = 5000;

    /**
     * Defines the message processing modes
     */
    public enum Mode {
        /**
         * Sets the message processing mode to its default behaviour which consists in parallel processing
         * the incoming messages. This mode favours performance over ordering meaning:
         * <br> Given message <b>A</b> is submitted
         * <br> Then message <b>B</b> is submitted
         * <br> Message <b>B</b> processing might complete before message <b>A</b>
         */
        DEFAULT,
        /**
         * Sets the message processing mode to wait pending. The submitted message will be processed only after
         * previously submitted messages have complete processing. The implementation gives an approximate guarantee
         * that the incoming message is actually processed after pending tasks.
         */
        WAIT_PENDING
    }

    private String type;
    private String id;
    private Mode mode;
    private Long waitFor;
    private String traceId;

    public ReceivedMessage() {
    }

    /**
     * The required message "type" field.
     * @return the value of the "type" field from the message.
     */
    @Override
    public String getType() {
        return type;
    }

    /**
     * A message may optionally include an id field, which on reply is sent as "reply_to"
     *
     * @return the id sent in the message, null if one was not sent.
     */
    public String getId() {
        return id;
    }

    /**
     * Defines the message processing mode. When nothing is supplied the {@link Mode#DEFAULT} is returned
     * @return the mode
     */
    public Mode getMode() {
        if (mode == null) {
            return Mode.DEFAULT;
        }
        return mode;
    }

    /**
     * Allow the client to define for how long the message processing should wait before the being submitted.
     * @return the waitFor default timer when value is not provided or when present the value provided by the client.
     */
    public Long getWaitFor() {
        switch (getMode()) {
            case WAIT_PENDING:
                // provide the default value when waitFor is not supplied
                if (waitFor == null) {
                    return WAIT_PENDING_TIMER;
                }

                // return the waitFor value when supplied correctly
                return waitFor;
            case DEFAULT:
            default:
                // there is no need to return waitFor here, any provided value will be ignored
                return null;
        }
    }

    public String getTraceId() {
        return traceId;
    }

    public ReceivedMessage setTraceId(final String traceId) {
        this.traceId = traceId;
        return this;
    }
}
