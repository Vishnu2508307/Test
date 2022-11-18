package com.smartsparrow.rtm.message.handler.iam;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.iam.service.AuthenticationService;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.iam.IESAuthorizeMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.sso.service.IESCredentials;
import com.smartsparrow.sso.service.IESWebSession;
import com.smartsparrow.sso.wiring.IESAuthentication;
import com.smartsparrow.util.DateFormat;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class IESAuthorizeMessageHandler implements MessageHandler<IESAuthorizeMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(IESAuthorizeMessageHandler.class);

    public static final String IES_AUTHORIZE = "ies.authorize";
    private static final String IES_AUTHORIZE_OK = "ies.authorize.ok";
    private static final String IES_AUTHORIZE_ERROR = "ies.authorize.error";

    private final Provider<MutableAuthenticationContext> mutableAuthenticationContextProvider;
    private final AuthenticationService<IESCredentials, IESWebSession> authenticationService;

    @Inject
    public IESAuthorizeMessageHandler(final Provider<MutableAuthenticationContext> mutableAuthenticationContextProvider,
                                      @IESAuthentication final AuthenticationService<IESCredentials, IESWebSession> authenticationService) {
        this.mutableAuthenticationContextProvider = mutableAuthenticationContextProvider;
        this.authenticationService = authenticationService;
    }

    @Override
    public void validate(IESAuthorizeMessage message) throws RTMValidationException {
        affirmArgument(message.getPearsonUid() != null, "pearsonUid is required");
        affirmArgument(message.getPearsonToken() != null, "pearsonToken is required");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = IES_AUTHORIZE)
    @Override
    public void handle(Session session, IESAuthorizeMessage message) throws WriteResponseException {
        final MutableAuthenticationContext mutableAuthenticationContext = mutableAuthenticationContextProvider.get();

        authenticationService.authenticate(new IESCredentials()
                .setPearsonUid(message.getPearsonUid())
                .setToken(message.getPearsonToken())
                .setInvalidBearerToken(null)) // add this in the future to support invalidation flow
                .map(iesWebSession -> {
                    // set the authentication context
                    mutableAuthenticationContext.setAuthenticationType(AuthenticationType.IES);
                    mutableAuthenticationContext.setPearsonToken(message.getPearsonToken());
                    mutableAuthenticationContext.setPearsonUid(message.getPearsonUid());
                    mutableAuthenticationContext.setAccount(iesWebSession.getAccount());
                    return iesWebSession;
                })
                // log any error in context
                .doOnEach(log.reactiveErrorThrowable("ies authorization error"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(iesWebSession -> {
                    // set the web token to the context
                    mutableAuthenticationContext.setWebToken(iesWebSession.getWebToken());
                    BasicResponseMessage responseMessage = new BasicResponseMessage(IES_AUTHORIZE_OK, message.getId());
                    // return the JWT as bearerToken so the frontend does not have to do the heavy lifting of
                    // managing different tokens
                    responseMessage.addField("bearerToken", message.getPearsonToken());
                    responseMessage.addField("expiry", DateFormat.asRFC1123(iesWebSession.getWebToken().getValidUntilTs()));
                    // write the response to the client
                    Responses.writeReactive(session, responseMessage);
                }, ex -> {
                    // write the error to the client
                    Responses.errorReactive(session, message.getId(), IES_AUTHORIZE_ERROR,
                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "ies authorization error");
                });
    }
}
