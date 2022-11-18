package com.smartsparrow.rtm.message.handler.iam;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.iam.service.AuthenticationType;
import com.smartsparrow.util.log.ReactiveMdc;
import org.eclipse.jetty.websocket.api.Session;

import com.google.common.base.Strings;
import com.smartsparrow.exception.ConflictException;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountAdapter;
import com.smartsparrow.iam.service.AccountAvatar;
import com.smartsparrow.iam.service.AccountProvisionSource;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.service.SubscriptionPermissionService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.iam.AccountProvisionMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.Emails;
import com.smartsparrow.util.Hashing;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import java.util.HashMap;

public class AccountProvisionMessageHandler implements MessageHandler<AccountProvisionMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AccountProvisionMessageHandler.class);

    public static final String IAM_INSTRUCTOR_PROVISION = "iam.instructor.provision";
    public static final String IAM_STUDENT_PROVISION = "iam.student.provision";

    private final AccountService accountService;
    private final SubscriptionPermissionService subscriptionPermissionService;

    @Inject
    public AccountProvisionMessageHandler(AccountService accountService,
                                          SubscriptionPermissionService subscriptionPermissionService) {
        this.accountService = accountService;
        this.subscriptionPermissionService = subscriptionPermissionService;
    }

    /**
     * Provision a new user under a new subscription. When the {@link this#IAM_INSTRUCTOR_PROVISION} type is used, the
     * account is provided with roles ADMIN, INSTRUCTOR, STUDENT, DEVELOPER. Additionally a new subscription permission
     * is saved marking the created user as the subscription OWNER
     *
     * @param session the websocket session
     * @param message the newly arrived message
     * @throws WriteResponseException when writing the message on the webSocket fails
     */
    @Trace(dispatcher = true, nameTransaction = false, metricName = "iam.provision")
    @Override
    public void handle(Session session, AccountProvisionMessage message) throws WriteResponseException {

        AccountAdapter accountAdapter;

        try {
            accountAdapter = accountService.provision(AccountProvisionSource.RTM, message.getHonorificPrefix(), message.getGivenName(),
                    message.getFamilyName(), message.getHonorificSuffix(), message.getEmail(), message.getPassword(),
                    message.getAffiliation(), message.getJobTitle(), IAM_INSTRUCTOR_PROVISION.equals(message.getType()), AuthenticationType.BRONTE);

            Account account = accountAdapter.getAccount();

            subscriptionPermissionService
                    .saveAccountPermission(account.getId(), account.getSubscriptionId(), PermissionLevel.OWNER)
                    .doOnEach(log.reactiveErrorThrowable("exception while saving account permission"))
                    .doOnEach(ReactiveTransaction.linkOnNext())
                    .doOnEach(ReactiveTransaction.expireOnComplete())
                    .subscriberContext(ReactiveMonitoring.createContext())
                    .blockLast();
        } catch (ConflictException e) {
            log.jsonDebug("user cannot be created", new HashMap<String, Object>() {
                {
                    put("hashEmail", Hashing.email(message.getEmail()));
                    put("error", e.getStackTrace());
                }
            });
            emitFailure(session, message, "email already in use", e.getResponseStatusCode());
            return;
        }

        emitSuccess(session, message, AccountPayload.from(accountAdapter.getAccount(),
                accountAdapter.getIdentityAttributes(),
                new AccountAvatar(), AuthenticationType.BRONTE));
    }

    /**
     * Validate the required field for this message
     *
     * @param message the received message that requires to be validated
     * @throws RTMValidationException when either email or password are not supplied or the email has an
     * invalid format
     */
    @Override
    public void validate(AccountProvisionMessage message) throws RTMValidationException {
        if (Strings.isNullOrEmpty(message.getEmail()) || Strings.isNullOrEmpty(message.getPassword())) {
            throw new RTMValidationException("`email` and `password` are required", message.getId(),
                    String.format("%s.error", message.getType()));
        }

        if (Emails.isNotValid(message.getEmail())) {
            throw new RTMValidationException("Invalid email supplied", message.getId(),
                    String.format("%s.error", message.getType()));
        }
    }

    /**
     * Emit an ok message
     *
     * @param session the session to write to
     * @param message the received message
     * @param payload the created account converted to an {@link AccountPayload}
     *
     * @throws WriteResponseException when unable to write to the session
     */
    private void emitSuccess(Session session, AccountProvisionMessage message, AccountPayload payload)
            throws WriteResponseException {
        BasicResponseMessage responseMessage = new BasicResponseMessage(String.format("%s.ok", message.getType()),
                message.getId());

        responseMessage.addField("account", payload);

        Responses.write(session, responseMessage);
    }

    /**
     * Emit a failure error message.
     *
     * @param session the session to write to
     * @param message the received message
     * @param details message with details what is going wrong
     *
     * @throws WriteResponseException when unable to write to the session
     */
    private void emitFailure(Session session, AccountProvisionMessage message, String details, int code)
            throws WriteResponseException {
        BasicResponseMessage responseMessage = new BasicResponseMessage(String.format("%s.error", message.getType()), message.getId());
        responseMessage.addField("reason", "Unable to create user: " + details);
        responseMessage.setCode(code);
        Responses.write(session, responseMessage);
    }

}
