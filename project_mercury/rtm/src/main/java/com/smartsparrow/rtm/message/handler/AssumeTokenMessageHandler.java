package com.smartsparrow.rtm.message.handler;

import static com.smartsparrow.rtm.util.Responses.errorReactive;
import static com.smartsparrow.rtm.util.Responses.writeReactive;
import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.time.Duration;
import java.util.UUID;

import javax.inject.Inject;

import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountLogEntry;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.CredentialService;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.iam.AssumeTokenMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.util.DateFormat;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

/**
 * Create a message to allow a user to authenticate for a short time period as another user.
 *
 * This is done by generating a new token. It is intended that this new token be used on a new connection, such as for
 * HTTP services which are not exposed via RTM or using it by connecting to a new WebSocket.
 *
 * General Rules:
 *  - The generated token should be short lived with a TTL of ~5 minutes.
 *  - Only support level users can do this.
 *  - Users can not assume another Support level user
 *  - Future: Instructors can assume the token of a Student.
 *  - Future: Admin can assume the token of an Instructor.
 *
 */
public class AssumeTokenMessageHandler implements MessageHandler<AssumeTokenMessage> {

    public static final String IAM_ASSUME_TOKEN = "iam.assume.token";
    public static final String IAM_ASSUME_TOKEN_OK = "iam.assume.token.ok";
    public static final String IAM_ASSUME_TOKEN_ERROR = "iam.assume.token.error";

    private final AuthenticationContextProvider authenticationContextProvider;
    private final AccountService accountService;
    private final CredentialService credentialService;

    @Inject
    public AssumeTokenMessageHandler(final AuthenticationContextProvider authenticationContextProvider,
                                     final AccountService accountService,
                                     final CredentialService credentialService) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.accountService = accountService;
        this.credentialService = credentialService;
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = IAM_ASSUME_TOKEN)
    public void validate(final AssumeTokenMessage message) throws RTMValidationException {
        affirmNotNull(message.getAccountId(), "account id is required");

        // Should not be able to generate a token for:
        // 1. A non-existent user
        // 2. Another support user
        Account account = accountService.findById(message.getAccountId())
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .blockFirst();
        if (account == null || account.getRoles().contains(AccountRole.SUPPORT)) {
            //
            throw new RTMValidationException("account id invalid",
                                             message.getId(),
                                             IAM_ASSUME_TOKEN_ERROR);
        }
    }

    @Override
    public void handle(final Session session, final AssumeTokenMessage message) throws WriteResponseException {

        final UUID requestorId = authenticationContextProvider.get().getAccount().getId();

        // Generate a short lived token for the supplied user
        credentialService.createWebSessionToken(message.getAccountId(),
                                                Duration.ofMinutes(5),
                                                null,
                                                null)
                // create a log entry about this.
                .doOnNext(token -> accountService.addLogEntry(message.getAccountId(),
                                                              AccountLogEntry.Action.BEARER_TOKEN_GENERATED,
                                                              requestorId,
                                                              "credential assumed"))
                // and the reverse entry
                .doOnNext(token -> accountService.addLogEntry(requestorId,
                                                              AccountLogEntry.Action.BEARER_TOKEN_GENERATED,
                                                              message.getAccountId(),
                                                              "assumed credential"))
                .subscribe(token -> {
                    // Write the response.
                    BasicResponseMessage responseMessage = new BasicResponseMessage(IAM_ASSUME_TOKEN_OK,
                                                                                    message.getId());
                    responseMessage.addField("bearerToken", token.getToken());
                    responseMessage.addField("expiry", DateFormat.asRFC1123(token.getValidUntilTs()));
                    writeReactive(session, responseMessage);
                }, error -> errorReactive(session, message.getId(), IAM_ASSUME_TOKEN_ERROR, error));
    }

}
