package com.smartsparrow.sso.service;

import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.base.Throwables;
import com.smartsparrow.sso.data.oidc.LogEventGateway;

import reactor.core.publisher.Flux;

/**
 * Log events related to the OIDC flow; the OIDC State parameter is used as the Session ID.
 *
 * A typical log for the flow will log:
 *  - The redirect operation (and the full URL with parameters we redirected to)
 *  - The callback operation start and parameters
 *  - User provisioning operations
 *  - All errors along the way.
 *
 * A debug log will include many other details, some of which may only remain for a certain TTL.
 *
 */
@Singleton
public class OpenIDConnectLogService {

    private static final long LOG_SENSITIVE_TTL = TimeUnit.DAYS.toSeconds(3);
    //
    private final LogEventGateway logEventGateway;

    @Inject
    public OpenIDConnectLogService(LogEventGateway logEventGateway) {
        this.logEventGateway = logEventGateway;
    }

    /**
     * Log an error to a session
     *
     * @param sessionId the session identifier (ie. the OIDC state)
     * @param message a message about what happened
     * @param t the exception that was raised
     * @return a Flux result, callers should block or add it as part of the reactive flow
     */
    public Flux<Void> logError(final String sessionId, final String message, final Throwable t) {
        return Flux.just(message) //
                // improve the log message
                .map(msg -> String.format("%s cause: %s", msg, Throwables.getStackTraceAsString(t)))
                // send it to processing.
                .flatMap(logMsg -> logEvent(sessionId, OpenIDConnectLogEvent.Action.ERROR, logMsg));
    }

    public Flux<Void> logError(final String sessionId, final String message) {
        return logEvent(sessionId, OpenIDConnectLogEvent.Action.ERROR, message);
    }

    /**
     * Log an action
     *
     * @param sessionId the session identifier (ie. the OIDC state)
     * @param action what happened
     * @return a Flux result, callers should block or add it as part of reactive flow
     */
    public Flux<Void> logEvent(final String sessionId, final OpenIDConnectLogEvent.Action action) {
        //
        return logEvent(sessionId, action, null, null);
    }

    /**
     * Log an action
     *
     * @param sessionId the session identifier (ie. the OIDC state)
     * @param action what happened
     * @param message a message about what happened
     * @return a Flux result, callers should block or add it as part of reactive flow
     */
    public Flux<Void> logEvent(final String sessionId,
            final OpenIDConnectLogEvent.Action action,
            @Nullable final String message) {
        //
        return logEvent(sessionId, action, message, null);
    }

    /**
     * Log an action which contains sensitive data. This data will removed after LOG_SENSITIVE_TTL.
     *
     * @param sessionId the session identifier (ie. the OIDC state)
     * @param action what happened
     * @param message a message about what happened
     * @return a Flux result, callers should block or add it as part of reactive flow
     */
    public Flux<Void> logEventSensitive(final String sessionId,
            final OpenIDConnectLogEvent.Action action,
            @Nullable final String message) {
        //
        return logEvent(sessionId, action, message, (int) LOG_SENSITIVE_TTL);
    }

    /**
     * Perform the actual logging operation
     *
     * @param sessionId the session id
     * @param action what happened
     * @param message a message about what happened
     * @param ttl an optional TTL in which to preserve the data, can be null.
     *
     * @return a Flux
     */
    Flux<Void> logEvent(final String sessionId,
            final OpenIDConnectLogEvent.Action action,
            @Nullable final String message,
            @Nullable final Integer ttl) {
        //
        affirmArgumentNotNullOrEmpty(sessionId, "sessionId is required");
        affirmNotNull(action, "action is required");
        //
        OpenIDConnectLogEvent e = new OpenIDConnectLogEvent() //
                .setSessionId(sessionId) // use the state param as the session id.
                .setId(UUIDs.timeBased()) //
                .setAction(action) //
                .setMessage(message) //
                .setTtl(ttl);

        return logEventGateway.persist(e);
    }

}
