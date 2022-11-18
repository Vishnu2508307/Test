package com.smartsparrow.rtm.message.handler.iam;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.common.base.Strings;
import com.google.inject.Provider;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.RTMWebSocketHandlerException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.iam.EditRoleMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.Enums;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class RemoveRoleMessageHandler implements MessageHandler<EditRoleMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(RemoveRoleMessageHandler.class);
    public static final String IAM_REMOVE_ROLE = "iam.role.remove";
    static final String IAM_REOMVE_ROLE_OK = "iam.role.remove.ok";
    static final String IAM_REMOVE_ROLE_ERROR = "iam.role.remove.error";

    private final Provider<MutableAuthenticationContext> authenticationContextProvider;
    private final AccountService accountService;

    @Inject
    public RemoveRoleMessageHandler(Provider<MutableAuthenticationContext> authenticationContextProvider,
                             AccountService accountService) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.accountService = accountService;
    }

    /**
     * Handle the incoming {@link EditRoleMessage}
     * Check that {@link EditRoleMessage#getAccountId()} and {@link EditRoleMessage#getRole()} are supplied and valid.
     * Emit an error on the web socket when the account is not found,
     * the account does not have to role to remove or the account only has one role (which is required).
     * Emit a success message when the role is removed and returns the full list of roles in the message response.
     *
     * @param session the web socket session
     * @param message the newly arrived message
     * @throws WriteResponseException when failing to write the message on the web socket
     */
    @SuppressWarnings("Duplicates")
    @Trace(dispatcher = true, nameTransaction = false, metricName = IAM_REMOVE_ROLE)
    @Override
    public void handle(Session session, EditRoleMessage message) throws WriteResponseException {
        AccountRole accountRole = Enums.of(AccountRole.class, message.getRole());

        Account target = accountService.findById(message.getAccountId())
                .doOnEach(log.reactiveErrorThrowable("exception while fetching the account"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .blockLast();
        Account current = authenticationContextProvider.get().getAccount();

        // when account is not found emit an error
        if (target == null) {
            log.warn("account not found");
            String error = String.format("account not found for id %s", message.getAccountId());
            emitError(session, message.getId(), error, HttpStatus.NOT_FOUND_404);
            return;
        }

        Set<AccountRole> roles = target.getRoles();

        // when the account does not have the role emit an error
        if (!roles.contains(accountRole)) {
            log.jsonDebug("account does not contain  role", new HashMap<String, Object>() {
                {
                    put("accountId",  message.getAccountId());
                    put("role",  accountRole);
                }
            });
            String error = String.format("account %s does not contain role %s", message.getAccountId(), String.valueOf(accountRole));
            emitError(session, message.getId(), error, HttpStatus.BAD_REQUEST_400);
            return;
        }

        // when the account only has one role (one is required) emit an error
        if (roles.size() == 1) {
            String error = String.format("cannot remove role, account %s has only 1 role", message.getAccountId());
            emitError(session, message.getId(), error, HttpStatus.UNPROCESSABLE_ENTITY_422);
            return;
        }
        // TODO: it should send an event to update all the authentication context
        accountService.removeRole(current.getId(), accountRole, target.getId());
        roles.remove(accountRole);
        emitSuccess(session, message.getId(), roles, target.getId());
    }

    /**
     * Validate the incoming message.
     *
     * @param message the received message that requires to be validated
     * @throws RTMWebSocketHandlerException when either accountId or/and role are missing, role does not contain a
     * valid value, accountId does not contain a valid value or the account is not found
     */
    @SuppressWarnings("Duplicates")
    @Override
    public void validate(EditRoleMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getAccountId() != null, "accountId is required");
            checkArgument(!Strings.isNullOrEmpty(message.getRole()), "role is required");
            checkArgument(!message.getRole().equals("INSTRUCTOR"), String.format("Deprecated `%s` role supplied", message.getRole()));

            try {
                Enums.of(AccountRole.class, message.getRole());
            } catch (IllegalArgumentException e) {
                throw new RTMValidationException(String.format("Unknown `%s` role supplied", message.getRole()),
                        message.getId(), IAM_REMOVE_ROLE_ERROR);
            }

            String accountError = String.format("account not found for id %s", message.getAccountId());
            checkArgument(accountService.findById(message.getAccountId()).blockLast() != null, accountError);
        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), IAM_REMOVE_ROLE_ERROR);
        }
    }

    private void emitSuccess(Session session, String inMessageId, Set<AccountRole> roles, UUID accountId)
            throws WriteResponseException {
        BasicResponseMessage responseMessage = new BasicResponseMessage(IAM_REOMVE_ROLE_OK, inMessageId);
        responseMessage.addField("accountId", accountId);
        responseMessage.addField("roles", roles);
        Responses.write(session, responseMessage);
    }

    private void emitError(Session session, String inMessageId, String error, int status) throws WriteResponseException {
        BasicResponseMessage responseMessage = new BasicResponseMessage(IAM_REMOVE_ROLE_ERROR, inMessageId);
        responseMessage.addField("reason", error);
        responseMessage.setCode(status);
        Responses.write(session, responseMessage);
    }
}
