package com.smartsparrow.sso.service;

import com.smartsparrow.iam.service.Credentials;

/**
 * Basic representation of LTI Consumer credentials. (Note that from version 1.3 an LTIConsumer is known as an
 * LTIPlatform)
 */
public interface LTIConsumerCredentials extends Credentials {

    /**
     * @return the LTI version
     */
    LTIVersion getLTIVersion();
}
