package com.smartsparrow.learner.lang;

import java.util.Collection;

public class InvalidFieldsException extends RuntimeException {

    private final Collection<String> invalidFields;

    public InvalidFieldsException(String message, Collection<String> invalidFields) {
        super(String.format("%s:%s", message, invalidFields));
        this.invalidFields = invalidFields;
    }

    public Collection<String> getInvalidFields() {
        return invalidFields;
    }
}
