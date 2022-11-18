package com.smartsparrow.rtm.message.handler.iam;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.eclipse.jetty.websocket.api.Session;

import com.google.common.base.Strings;
import com.google.inject.Provider;
import com.smartsparrow.exception.ConflictException;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountAdapter;
import com.smartsparrow.iam.service.AccountAvatar;
import com.smartsparrow.iam.service.AccountProvisionSource;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.iam.AccountSubscriptionProvisionMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.iam.IamAccountProvisionRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.Emails;
import com.smartsparrow.util.Enums;
import com.smartsparrow.util.Hashing;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class SubscriptionAccountProvisionMessageHandler implements MessageHandler<AccountSubscriptionProvisionMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(SubscriptionAccountProvisionMessageHandler.class);

    public static final String IAM_SUBSCRIPTION_USER_PROVISION = "iam.subscription.user.provision";
    private static final String IAM_SUBSCRIPTION_USER_PROVISION_ERROR = "iam.subscription.user.provision.error";
    private static final String IAM_SUBSCRIPTION_USER_PROVISION_OK = "iam.subscription.user.provision.ok";

    private final AccountService accountService;
    private final Provider<MutableAuthenticationContext> authenticationContextProvider;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final IamAccountProvisionRTMProducer iamAccountProvisionRTMProducer;

    @Inject
    public SubscriptionAccountProvisionMessageHandler(AccountService accountService,
                                                      Provider<MutableAuthenticationContext> authenticationContextProvider,
                                                      Provider<RTMClientContext> rtmClientContextProvider,
                                                      IamAccountProvisionRTMProducer iamAccountProvisionRTMProducer){
        this.accountService = accountService;
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.iamAccountProvisionRTMProducer = iamAccountProvisionRTMProducer;
    }

    @Override
    public void handle(Session session, AccountSubscriptionProvisionMessage message) throws WriteResponseException {
        Account account = authenticationContextProvider.get().getAccount();
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();

        AccountAdapter accountAdapter;

        Set<AccountRole> roles = message.getRoles()
                .stream()
                .map(one-> Enums.of(AccountRole.class, one))
                .collect(Collectors.toSet());

        try {
            accountAdapter = accountService.provision(AccountProvisionSource.RTM,
                                                      account.getSubscriptionId(),
                                                      roles,
                                                      message.getHonorificPrefix(),
                                                      message.getGivenName(),
                                                      message.getFamilyName(),
                                                      message.getHonorificSuffix(),
                                                      message.getEmail(),
                                                      message.getPassword(),
                                                      false,
                                                      message.getAffiliation(),
                                                      message.getJobTitle(),
                                                      AuthenticationType.BRONTE);
        } catch (ConflictException e) {
            log.jsonDebug("User cannot be created", new HashMap<String, Object>() {
                {
                    put("hashEmail", Hashing.email(message.getEmail()));
                    put("error", e.getStackTrace());
                }
            });
            emitFailure(session, e.getResponseStatusCode(), message.getId(), e.getMessage());
            return;
        }

        emitSuccess(session, AccountPayload.from(accountAdapter.getAccount(),
                                                 accountAdapter.getIdentityAttributes(), new AccountAvatar(),
                                                 AuthenticationType.BRONTE), message.getId());
        // produces consumable event
        iamAccountProvisionRTMProducer.buildAccountProvisionedRTMConsumable(rtmClientContext,
                                                                            accountAdapter.getAccount().getSubscriptionId(), accountAdapter.getAccount().getId()).produce();
    }

    /**
     * Checks that a valid email and password are provided with the message. It also check that at least 1 role is
     * supplied as a message parameter. Roles values are also validated.
     *
     * @param message the received message that requires to be validated
     * @throws RTMValidationException when the email is empty or invalid, the password is empty, roles are empty
     * or contains invalid/redundant values or the number of roles exceed the {@link AccountRole} enum values count
     */
    @Override
    public void validate(AccountSubscriptionProvisionMessage message) throws RTMValidationException {
        if (Strings.isNullOrEmpty(message.getEmail()) || Strings.isNullOrEmpty(message.getPassword())) {
            throw new RTMValidationException("Email and password are required", message.getId(),
                                             IAM_SUBSCRIPTION_USER_PROVISION_ERROR);
        }

        if (Emails.isNotValid(message.getEmail())) {
            throw new RTMValidationException("Email is not valid", message.getId(),
                                             IAM_SUBSCRIPTION_USER_PROVISION_ERROR);
        }

        if (message.getRoles() == null || message.getRoles().isEmpty()) {
            throw new RTMValidationException("At least one role is required", message.getId(),
                                             IAM_SUBSCRIPTION_USER_PROVISION_ERROR);
        }

        if (message.getRoles().size() > AccountRole.values().length) {
            throw new RTMValidationException("Too many roles supplied", message.getId(),
                                             IAM_SUBSCRIPTION_USER_PROVISION_ERROR);
        }

        boolean messageContainsRestrictedRoles = message.getRoles()
                .stream()
                .anyMatch(AccountRole::isRestricted);

        if (messageContainsRestrictedRoles) {
            throw new RTMValidationException("Invalid role/s supplied", message.getId(), IAM_SUBSCRIPTION_USER_PROVISION_ERROR);
        }

        for (String role : message.getRoles()) {
            try {
                Enums.of(AccountRole.class, role);
            } catch (IllegalArgumentException iae) {
                throw new RTMValidationException(String.format("Unknown `%s` role supplied", role), message.getId(),
                                                 IAM_SUBSCRIPTION_USER_PROVISION_ERROR);
            }
        }
    }

    private void emitSuccess(Session session, @Nonnull AccountPayload payload, String inMessageId) throws WriteResponseException {
        BasicResponseMessage message = new BasicResponseMessage(IAM_SUBSCRIPTION_USER_PROVISION_OK, inMessageId);
        message.addField("account", payload);
        Responses.write(session, message);
    }

    private void emitFailure(Session session, int code, String inMessageId, String errorMessage)
            throws WriteResponseException {
        BasicResponseMessage message = new BasicResponseMessage(IAM_SUBSCRIPTION_USER_PROVISION_ERROR, code, inMessageId);
        message.addField("reason", errorMessage);
        Responses.write(session, message);
    }
}
