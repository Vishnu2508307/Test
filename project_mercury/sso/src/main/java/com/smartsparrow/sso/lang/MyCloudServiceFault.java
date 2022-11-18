package com.smartsparrow.sso.lang;

import com.smartsparrow.exception.Fault;
import org.apache.http.HttpStatus;

public class MyCloudServiceFault extends Fault {

    public MyCloudServiceFault(String message) {
        super(message);
    }

    @Override
    public int getResponseStatusCode() {
        return HttpStatus.SC_BAD_GATEWAY;
    }

    @Override
    public String getType() {
        return "BAD_GATEWAY";
    }
}
