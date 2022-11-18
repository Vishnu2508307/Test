package com.smartsparrow.lang;

import com.smartsparrow.exception.Fault;

public class S3ReadFault extends Fault {

    private static final long serialVersionUID = 6181856916654072903L;

    public S3ReadFault(String message) {
        super(message);
    }

    @Override
    public int getResponseStatusCode() {
        return 422;
    }

    @Override
    public String getType() {
        return "UNPROCESSABLE_ENTITY";
    }
}
