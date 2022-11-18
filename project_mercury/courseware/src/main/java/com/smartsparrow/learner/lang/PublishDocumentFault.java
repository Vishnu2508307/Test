package com.smartsparrow.learner.lang;

import com.smartsparrow.exception.Fault;

public class PublishDocumentFault extends Fault {

    public PublishDocumentFault(String message) {
        super(message);
    }

    @Override
    public int getResponseStatusCode() {
        return 500;
    }

    @Override
    public String getType() {
        return "INTERNAL_SERVER_ERROR";
    }
}
