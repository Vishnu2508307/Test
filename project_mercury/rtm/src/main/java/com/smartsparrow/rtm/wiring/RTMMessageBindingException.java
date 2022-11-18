package com.smartsparrow.rtm.wiring;

public class RTMMessageBindingException extends RuntimeException {

    private static final long serialVersionUID = -1363874531107280177L;

    RTMMessageBindingException(String typeClassName, String genericTypeName) {
        super(String.format("Class `%s` is not assignable to class with parameterized type `%s`", typeClassName,
                genericTypeName));
    }

    RTMMessageBindingException(String message, Throwable cause) {
        super(message, cause);
    }
}
