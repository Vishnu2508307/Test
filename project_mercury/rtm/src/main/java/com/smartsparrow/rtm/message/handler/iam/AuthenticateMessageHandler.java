package com.smartsparrow.rtm.message.handler.iam;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.common.base.Strings;
import com.google.inject.Provider;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.config.ConfigurableFeatureValues;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AccountShadowAttributeName;
import com.smartsparrow.iam.service.AuthenticationService;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.BronteCredentials;
import com.smartsparrow.iam.service.BronteWebSession;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.service.WebSession;
import com.smartsparrow.iam.wiring.BronteAuthentication;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.iam.AuthenticateMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.sso.service.IESCredentials;
import com.smartsparrow.sso.service.IESWebSession;
import com.smartsparrow.sso.service.MyCloudCredentials;
import com.smartsparrow.sso.service.MyCloudWebSession;
import com.smartsparrow.sso.wiring.IESAuthentication;
import com.smartsparrow.sso.wiring.MyCloudAuthentication;
import com.smartsparrow.util.DateFormat;
import com.smartsparrow.util.Maps;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

/**
 * Handle an authentication message.
 */
public class AuthenticateMessageHandler implements MessageHandler<AuthenticateMessage> {

    public static final String AUTHENTICATE = "authenticate";
    private static final String AUTHENTICATE_OK = "authenticate.ok";
    private static final String AUTHENTICATE_ERROR = "authenticate.error";

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AuthenticateMessageHandler.class);

    private final Provider<MutableAuthenticationContext> authenticationContextProvider;
    private final AuthenticationService<BronteCredentials, BronteWebSession> bronteAuthenticationService;
    private final AuthenticationService<IESCredentials, IESWebSession> iesAuthenticationService;
    private final AuthenticationService<MyCloudCredentials, MyCloudWebSession> myCloudAuthenticationService;
    private final AccountService accountService;

    /**
     * Construct an authenticate message handler.
     *  @param authenticationContextProvider this handler mutates this state, so inject it.
     * @param bronteAuthenticationService   the bronte authentication
     * @param iesAuthenticationService      the ies authentication
     * @param myCloudAuthenticationService  the my cloud authentication
     * @param accountService
     */
    @Inject
    public AuthenticateMessageHandler(final Provider<MutableAuthenticationContext> authenticationContextProvider,
                                      @BronteAuthentication final AuthenticationService<BronteCredentials, BronteWebSession> bronteAuthenticationService,
                                      @IESAuthentication final AuthenticationService<IESCredentials, IESWebSession> iesAuthenticationService,
                                      @MyCloudAuthentication final AuthenticationService<MyCloudCredentials, MyCloudWebSession> myCloudAuthenticationService,
                                      AccountService accountService) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.bronteAuthenticationService = bronteAuthenticationService;
        this.iesAuthenticationService = iesAuthenticationService;
        this.myCloudAuthenticationService = myCloudAuthenticationService;
        this.accountService = accountService;
    }

    @Override
    public void validate(AuthenticateMessage message) throws RTMValidationException {

        boolean hasToken = message.getBearerToken() != null;
        boolean hasEmailAndPassword = message.getEmail() != null && message.getPassword() != null;

        affirmArgument(hasToken || hasEmailAndPassword,
                       "either bearerToken or email and password are required");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHENTICATE)
    @Override
    public void handle(Session session, AuthenticateMessage message) throws WriteResponseException {

        MutableAuthenticationContext mutableAuthenticationContext = authenticationContextProvider.get();

        Mono<WebSession> webSessionMono;

        webSessionMono = bronteAuthenticationService.authenticate(new BronteCredentials()
                                                                       .setBearerToken(message.getBearerToken())
                                                                       .setEmail(message.getEmail())
                                                                       .setPassword(message.getPassword()))
        .map(bronteWebSession -> {
            mutableAuthenticationContext.setAuthenticationType(AuthenticationType.BRONTE);
            mutableAuthenticationContext.setAccount(bronteWebSession.getAccount());
            return bronteWebSession;
        });

        // FIXME: this is a temporary behaviour since the frontend does not support distinguishing between
        // FIXME: a mercury bearerToken and an ies JWT and a myCloud CTS
        // check with IES and MyCloud authentication if bearerToken is not null and above Bronte authentication fails
        if (!Strings.isNullOrEmpty(message.getBearerToken())) {
            // try an IES authentication with bearer token if the webSessionTokenMono is empty
            webSessionMono = webSessionMono.switchIfEmpty(Mono.defer(() -> iesAuthenticationService.authenticate(message.getBearerToken())
                    .map(iesWebSession -> {
                        // set the authentication context
                        mutableAuthenticationContext.setAuthenticationType(AuthenticationType.IES);
                        mutableAuthenticationContext.setPearsonToken(message.getBearerToken());
                        mutableAuthenticationContext.setPearsonUid(iesWebSession.getWebToken().getPearsonUid());
                        mutableAuthenticationContext.setAccount(iesWebSession.getAccount());
                        return iesWebSession;
                    })))
            // try a myCloud authentication with bearer token
           .switchIfEmpty(Mono.defer(() -> myCloudAuthenticationService.authenticate(message.getBearerToken())
                   .map(myCloudWebSession -> {
                       // set the authentication context
                       mutableAuthenticationContext.setAuthenticationType(AuthenticationType.MYCLOUD);
                       mutableAuthenticationContext.setPearsonToken(message.getBearerToken());
                       mutableAuthenticationContext.setPearsonUid(myCloudWebSession.getWebToken().getPearsonUid());
                       mutableAuthenticationContext.setAccount(myCloudWebSession.getAccount());
                       return myCloudWebSession;
                   })));
        }

        webSessionMono
                // in theory we should never get here
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundFault("Error acquiring web session token"))))
                // log any error in context
                .doOnEach(log.reactiveErrorThrowable("authentication error"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                // load the account shadow attribute for the evaluation feature if found
                .flatMap(webSession -> accountService.findShadowAttribute(webSession.getAccount(), AccountShadowAttributeName.REACTIVE_EVALUATION)
                        .map(accountShadowAttribute -> {
                            mutableAuthenticationContext.setConfiguredFeatures(Maps.of(ConfigurableFeatureValues.EVALUATION, accountShadowAttribute.getAttribute()));
                            return accountShadowAttribute;
                        })
                        .then(Mono.just(webSession))
                        .onErrorResume(NotFoundFault.class, ex -> Mono.just(webSession))
                )
                .subscribe(webSession -> {
                    // set the web token to the context
                    mutableAuthenticationContext.setWebToken(webSession.getWebToken());
                    BasicResponseMessage responseMessage = new BasicResponseMessage(AUTHENTICATE_OK, message.getId());
                    // set bearer token either from input message or from web session token
                    String bearerToken = (message.getBearerToken() != null ? message.getBearerToken() : webSession.getWebToken().getToken());
                    responseMessage.addField("bearerToken",bearerToken);
                    responseMessage.addField("expiry", DateFormat.asRFC1123(webSession.getWebToken().getValidUntilTs()));
                    // write the response to the client
                    Responses.writeReactive(session, responseMessage);
                }, ex -> {
                    log.jsonError("Unable to authenticate {}",
                                  new HashMap<String, Object>() {
                                      {
                                          put("message", message.toString());
                                          put("error", ex.getStackTrace());
                                      }
                                  }, ex);
                    // write the error to the client
                    Responses.errorReactive(session, message.getId(), AUTHENTICATE_ERROR,
                                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "authentication error");
                });
    }
}
