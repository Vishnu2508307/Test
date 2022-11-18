package com.smartsparrow.rtm.message.handler;

import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;

import java.util.UUID;

import javax.inject.Inject;

import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.CreateOIDCProviderMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.sso.service.OpenIDConnectService;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

/**
 * This is message to add credentials for OpenID Connect
 */
public class CreateOIDCProviderMessageHandler implements MessageHandler<CreateOIDCProviderMessage> {

    public static final String IAM_OIDC_PROVIDER_CREATE = "iam.oidc.provider.create";
    public static final String IAM_OIDC_PROVIDER_CREATE_OK = "iam.oidc.provider.create.ok";
    public static final String IAM_OIDC_PROVIDER_CREATE_ERROR = "iam.oidc.provider.create.error";

    private final OpenIDConnectService openIDConnectService;
    private final AuthenticationContextProvider authenticationContextProvider;

    @Inject
    public CreateOIDCProviderMessageHandler(OpenIDConnectService openIDConnectService,
                                            AuthenticationContextProvider authenticationContextProvider) {
        this.openIDConnectService = openIDConnectService;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @Override
    public void validate(CreateOIDCProviderMessage message) throws RTMValidationException {
        affirmArgumentNotNullOrEmpty(message.getIssuerUrl(), "issuerUrl is required");
        affirmArgumentNotNullOrEmpty(message.getClientId(), "clientId is required");
        affirmArgumentNotNullOrEmpty(message.getClientSecret(), "clientSecret is required");
        affirmArgument(message.getRequestScope() == null || message.getRequestScope().startsWith("openid"),
                "invalid requestScope");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = IAM_OIDC_PROVIDER_CREATE)
    @Override
    public void handle(Session session, CreateOIDCProviderMessage message) {
        UUID subscriptionId = authenticationContextProvider.get().getAccount().getSubscriptionId();

        openIDConnectService.addCredential(subscriptionId, message.getIssuerUrl(), message.getClientId(),
                message.getClientSecret(), message.getRequestScope())
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(
                        credential -> {
                            BasicResponseMessage basicResponseMessage = new BasicResponseMessage(IAM_OIDC_PROVIDER_CREATE_OK, message.getId());
                            basicResponseMessage.addField("credential", credential);
                            Responses.writeReactive(session, basicResponseMessage);
                        },
                        ex -> Responses.errorReactive(session, message.getId(), IAM_OIDC_PROVIDER_CREATE_ERROR, ex));

    }
}
