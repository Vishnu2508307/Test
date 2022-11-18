package com.smartsparrow.rtm.message.handler.iam;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.iam.lang.UnauthorizedFault;
import com.smartsparrow.iam.service.AuthenticationService;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.iam.MyCloudAuthorizeMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.sso.lang.MyCloudServiceFault;
import com.smartsparrow.sso.service.MyCloudCredentials;
import com.smartsparrow.sso.service.MyCloudWebSession;
import com.smartsparrow.sso.wiring.MyCloudAuthentication;
import com.smartsparrow.util.DateFormat;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class MyCloudAuthorizeMessageHandler implements MessageHandler<MyCloudAuthorizeMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(MyCloudAuthorizeMessageHandler.class);

    public static final String MYCLOUD_AUTHORIZE = "mycloud.authorize";
    private static final String MYCLOUD_AUTHORIZE_OK = "mycloud.authorize.ok";
    private static final String MYCLOUD_AUTHORIZE_ERROR = "mycloud.authorize.error";

    private final Provider<MutableAuthenticationContext> mutableAuthenticationContextProvider;
    private final AuthenticationService<MyCloudCredentials, MyCloudWebSession> authenticationService;

    @Inject
    public MyCloudAuthorizeMessageHandler(
            final Provider<MutableAuthenticationContext> mutableAuthenticationContextProvider,
            @MyCloudAuthentication final AuthenticationService<MyCloudCredentials, MyCloudWebSession> authenticationService) {
        this.mutableAuthenticationContextProvider = mutableAuthenticationContextProvider;
        this.authenticationService = authenticationService;
    }

    @Override
    public void validate(MyCloudAuthorizeMessage message) throws RTMValidationException {
        affirmArgument(message.getMyCloudToken() != null, "myCloudToken is required");
    }

    @Trace(dispatcher = true)
    @Override
    public void handle(Session session, MyCloudAuthorizeMessage message) throws WriteResponseException {
        final MutableAuthenticationContext mutableAuthenticationContext = mutableAuthenticationContextProvider.get();

        authenticationService.authenticate(new MyCloudCredentials()
                                                   .setToken(message.getMyCloudToken()))
                .map(myCloudWebSession -> {
                    // set the authentication context
                    mutableAuthenticationContext.setAuthenticationType(AuthenticationType.MYCLOUD);
                    mutableAuthenticationContext.setPearsonToken(message.getMyCloudToken());
                    mutableAuthenticationContext.setPearsonUid(myCloudWebSession.getWebToken().getPearsonUid());
                    mutableAuthenticationContext.setAccount(myCloudWebSession.getAccount());
                    return myCloudWebSession;
                })
                // log any error in context
                .doOnEach(log.reactiveErrorThrowable("myCloud authorization error"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(webSessionToken -> {
                    // set the web token to the context
                    mutableAuthenticationContext.setWebToken(webSessionToken.getWebToken());
                    BasicResponseMessage responseMessage = new BasicResponseMessage(MYCLOUD_AUTHORIZE_OK,
                                                                                    message.getId());
                    // return the JWT as bearerToken so the frontend does not have to do the heavy lifting of
                    // managing different tokens
                    responseMessage.addField("bearerToken", message.getMyCloudToken());
                    responseMessage.addField("expiry",
                                             DateFormat.asRFC1123(webSessionToken.getWebToken().getValidUntilTs()));
                    // write the response to the client
                    Responses.writeReactive(session, responseMessage);
                }, ex -> {
                    // write the error to the client
                    if (ex instanceof UnauthorizedFault || ex instanceof MyCloudServiceFault) {
                        Responses.errorReactive(session, message.getId(), MYCLOUD_AUTHORIZE_ERROR, ex);
                    } else {
                        Responses.errorReactive(session, message.getId(), MYCLOUD_AUTHORIZE_ERROR,
                                                HttpStatus.SC_INTERNAL_SERVER_ERROR, "Unable to authorize user");
                    }
                });
    }
}
