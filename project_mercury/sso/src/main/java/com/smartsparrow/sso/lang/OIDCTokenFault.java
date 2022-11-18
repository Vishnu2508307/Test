package com.smartsparrow.sso.lang;

import com.smartsparrow.exception.IllegalArgumentFault;

public class OIDCTokenFault extends IllegalArgumentFault {

    public OIDCTokenFault(String message) {
        super(message);
    }
}
