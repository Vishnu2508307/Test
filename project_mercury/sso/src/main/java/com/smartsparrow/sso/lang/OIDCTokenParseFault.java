package com.smartsparrow.sso.lang;

import com.smartsparrow.exception.IllegalArgumentFault;

public class OIDCTokenParseFault extends IllegalArgumentFault {

    public OIDCTokenParseFault(String message) {
        super(message);
    }
}
