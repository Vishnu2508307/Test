package com.smartsparrow.rtm.lang;

import java.io.IOException;

/**
 * An exception which represents writing a response to the client.
 */
public class WriteResponseException extends IOException {

    public WriteResponseException(Throwable cause) {
        super(cause);
    }

    public WriteResponseException(String message, Throwable cause) {
        super(message, cause);
    }

}
