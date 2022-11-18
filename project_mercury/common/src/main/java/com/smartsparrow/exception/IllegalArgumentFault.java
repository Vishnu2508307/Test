package com.smartsparrow.exception;

/**
 * A fault to represent an illegal argument supplied to a remote-user.
 */
public class IllegalArgumentFault extends Fault {

    /**
     * Construct a new object with the specified message.
     * @param message
     */
    public IllegalArgumentFault(String message) {
        super(message);
    }

    @Override
    public int getResponseStatusCode() {
        return 400;
    }

    @Override
    public String getType() {
        return "BAD_REQUEST";
    }
}
