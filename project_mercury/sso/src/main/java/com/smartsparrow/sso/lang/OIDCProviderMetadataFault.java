package com.smartsparrow.sso.lang;

import com.smartsparrow.exception.IllegalArgumentFault;

public class OIDCProviderMetadataFault extends IllegalArgumentFault {

    public OIDCProviderMetadataFault(String message) {
        super(message);
    }
}
