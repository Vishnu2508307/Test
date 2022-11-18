package com.smartsparrow.exception;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * An internal extension to a RuntimeException in order to represent a condition that is business logic specific and
 * will generally be sent back to the remote client.
 *
 * For example:
 *  1. Not supplying an authentication token (or not authenticated);
 *  2. A general permission error (or not enrolled to a course);
 *  3. An invalid Zip file submitted as part of a plugin upload.
 *
 * It is not intended to wrap system failures, such as loss of connection to a database.
 *
 * These types of Exceptions:
 *  1. will immediately pop out of the call stack;
 *  2. are consumed by a high-level fault barrier or handler, which perform the necessary logging.
 *
 * See: https://www.oracle.com/technetwork/java/effective-exceptions-092345.html
 */
public abstract class Fault extends RuntimeException implements ErrorResponseType {

    private static final long serialVersionUID = 3435218633372623345L;

    @JsonIgnore
    private Map<String, Object> extensions = new HashMap<>();

    /**
     * Constructs a new Fault, with the same behaviour as a no-arg RuntimeException (i.e. a null message)
     */
    public Fault() {
        super();
    }

    /**
     * Construct a new Fault, with the specified message.
     *
     * @param message the detail message
     */
    public Fault(String message) {
        super(message);
    }

    //
    // intentionally not exposing constructors with Throwable values.
    // this is because the detailmessage of the exception will typically be sent to end-user at
    // which point it will include the exception class name, e.g. java.lang.IllegalArgumentException
    //


    /**
     * Add an extended error field.
     *
     * @param key the custom field
     * @param value the value of the field
     */
    @JsonAnySetter
    protected void addExtension(String key, Object value) {
        extensions.put(key, value);
    }

    /**
     * Get extended error fields.
     *
     * @return a map of extended error fields
     */
    @Override
    @JsonAnyGetter
    public Map<String, Object> getExtensions() {
        return extensions;
    }

}
