package com.smartsparrow.sso.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.exception.UnsupportedOperationFault;
import com.smartsparrow.util.Interpolator;
import com.smartsparrow.util.Resource;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class SessionJsTemplateInitializer {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(SessionJsTemplateInitializer.class);

    private static final String HASH = "hash";
    private static final String LAUNCH_REQUEST_ID = "launchRequestId";
    private static final String TEMPLATE = "initIESSession.html";


    @Inject
    public SessionJsTemplateInitializer() {
    }

    /**
     * Injects variables into the session js template. Used to try initializing an IES session from the backend
     *
     * @return the interpolated session js init template
     */
    public String get(final String hash, final UUID launchRequestId) {
        try {
            // load the template
            final String template = Resource.loadAsString(TEMPLATE);
            // seed the context
            Interpolator interpolator = new Interpolator()
                    .addVariable(HASH, hash)
                    .addVariable(LAUNCH_REQUEST_ID, launchRequestId.toString());
            // inject the template with interpolated values
            return interpolator.interpolate(template);
        } catch (IOException e) {
            log.jsonError("error initializing sessionJs init template", new HashMap<>(), e);
            throw new UnsupportedOperationFault("error initializing the session js init template");
        }
    }
}
