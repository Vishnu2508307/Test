package com.smartsparrow.lang;

import com.smartsparrow.exception.Fault;

public class S3UploadFault extends Fault {

    public S3UploadFault(String message) {
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
