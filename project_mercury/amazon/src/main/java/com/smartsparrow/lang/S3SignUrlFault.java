package com.smartsparrow.lang;

import com.smartsparrow.exception.Fault;

public class S3SignUrlFault extends Fault {

    public S3SignUrlFault(String message) {
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
