package com.smartsparrow.rtm.message.handler.iam;

import static com.google.common.base.Preconditions.checkArgument;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.iam.AccountSubscriptionMigrateMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import java.util.HashMap;

public class AccountSubscriptionMigrateMessageHandler implements MessageHandler<AccountSubscriptionMigrateMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AccountSubscriptionMigrateMessageHandler.class);

    public static final String IAM_ACCOUNT_SUBSCRIPTION_MIGRATE = "iam.account.subscription.migrate";
    private static final String IAM_ACCOUNT_SUBSCRIPTION_MIGRATE_OK = "iam.account.subscription.migrate.ok";
    private static final String IAM_ACCOUNT_SUBSCRIPTION_MIGRATE_ERROR = "iam.account.subscription.migrate.error";

    private final AccountService accountService;
    private final Provider<AuthenticationContext> authenticationContextProvider;

    @Inject
    public AccountSubscriptionMigrateMessageHandler(AccountService accountService,
            Provider<AuthenticationContext> authenticationContextProvider) {
        this.accountService = accountService;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    /**
     * Handle the migration of an account to a new subscription.
     *
     * @param session the websocket session
     * @param message the incoming message
     * @throws WriteResponseException when failing to write the response to the webSocket
     */
    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = IAM_ACCOUNT_SUBSCRIPTION_MIGRATE)
    public void handle(Session session, AccountSubscriptionMigrateMessage message) throws WriteResponseException {
        // FIXME: An event should be sent to update the account authenticationContext/mutableAuthenticationContext of the migratedAccount if logged in
        // TODO: this api should use the notification framework once implemented see https://docs.google.com/document/d/1EhBeWRnQJqU96_hh5NWRUD00x5sKx-Z4_fshJStn0yA/edit#
        // TODO: once notification events are integrated discuss if it's still a good idea to expose this api
        Account accountToMigrate = accountService.findById(message.getAccountId())
                .doOnEach(log.reactiveErrorThrowable("exception while fetching the account"))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .blockLast();

        if (accountToMigrate == null) {
            log.warn("account not found");
            emitError(session, message.getId(), HttpStatus.SC_NOT_FOUND, String.format("account not found for id `%s`", message.getAccountId()));
            return;
        }

        try {
            Account migrated = accountService.migrateAccountTo(accountToMigrate, authenticationContextProvider.get()
                    .getAccount().getSubscriptionId(), message.getRoles()).block();

            if (migrated == null) {
                log.warn("could not process the account migration request");
                emitError(session, message.getId(), HttpStatus.SC_UNPROCESSABLE_ENTITY, "An unexpected error occurred");
                return;
            }
            emitSuccess(session, message.getId(), accountService.getAccountPayload(migrated).block());
        } catch (IllegalArgumentException e) {
            log.jsonDebug("the request is not valid", new HashMap<String, Object>() {
                {
                    put("id",  message.getId());
                    put("error", e.getStackTrace());
                }
            });
            emitError(session, message.getId(), HttpStatus.SC_BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Validates that the required arguments are supplied and valid.
     * @param message the received message that requires to be validated
     * @throws RTMValidationException either {@link AccountSubscriptionMigrateMessage#getAccountId()} or
     * {@link AccountSubscriptionMigrateMessage#getRoles()} are null or the latter does not supply any role.
     */
    @Override
    public void validate(AccountSubscriptionMigrateMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getAccountId() != null, "accountId is required");
            checkArgument(message.getRoles() != null && !message.getRoles().isEmpty(),
                    "roles field required and should provide at least 1 role");

            boolean messageContainsRestrictedRoles = message.getRoles()
                    .stream()
                    .anyMatch(AccountRole.RESTRICTED_ROLES::contains);

            if (messageContainsRestrictedRoles) {
                throw new RTMValidationException("Invalid role/s supplied", message.getId(), IAM_ACCOUNT_SUBSCRIPTION_MIGRATE_ERROR);
            }
        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), IAM_ACCOUNT_SUBSCRIPTION_MIGRATE_ERROR);
        }
    }

    /**
     * Write a success response message on the webSocket.
     *
     * @param session the webSocket session
     * @param messageId keeps track of the reply to message
     * @param accountPayload an {@link AccountPayload} object
     * @throws WriteResponseException when failing to write message to the webSocket
     */
    private void emitSuccess(Session session, String messageId, AccountPayload accountPayload) throws WriteResponseException {
        BasicResponseMessage responseMessage = new BasicResponseMessage(IAM_ACCOUNT_SUBSCRIPTION_MIGRATE_OK, messageId);
        responseMessage.addField("account", accountPayload);
        Responses.write(session, responseMessage);
    }

    /**
     * Write an error response message on the webSocket.
     *
     * @param session the websocket session
     * @param messageId keeps track of the reply to message
     * @param code the message status code
     * @param error the error message
     * @throws WriteResponseException when failing to write to the webSocket
     */
    private void emitError(Session session, String messageId, int code, String error) throws WriteResponseException {
        BasicResponseMessage responseMessage = new BasicResponseMessage(IAM_ACCOUNT_SUBSCRIPTION_MIGRATE_ERROR, code, messageId);
        responseMessage.addField("reason", error);
        Responses.write(session, responseMessage);
    }
}
