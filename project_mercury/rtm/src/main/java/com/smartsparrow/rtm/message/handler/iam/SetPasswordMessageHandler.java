package com.smartsparrow.rtm.message.handler.iam;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.iam.SetPasswordMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.Passwords;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class SetPasswordMessageHandler implements MessageHandler<SetPasswordMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(SetPasswordMessageHandler.class);

    public static final String IAM_PASSWORD_SET = "iam.password.set";
    public static final String IAM_PASSWORD_SET_OK = "iam.password.set.ok";
    public static final String IAM_PASSWORD_SET_ERROR = "iam.password.set.error";

    private final AccountService accountService;
    private final AuthenticationContextProvider authenticationContextProvider;

    @Inject
    SetPasswordMessageHandler(AccountService accountService,
                              AuthenticationContextProvider authenticationContextProvider) {
        this.accountService = accountService;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @Override
    public void validate(SetPasswordMessage message) throws RTMValidationException {
        affirmArgument(message.getOldPassword() != null, "missing oldPassword");
        affirmArgument(message.getNewPassword() != null, "missing newPassword");
        affirmArgument(message.getConfirmNew() != null, "missing confirmNew");
        // Check the provided old password matches the existing user password
        final Account account = authenticationContextProvider.get().getAccount();
        affirmArgument(accountService.verifyPassword(message.getOldPassword(), account.getPasswordHash()), "provided password doesn't match existing password");
        // Check the new password matches the confirm new password
        affirmArgument(message.getNewPassword().equals(message.getConfirmNew()), "new password does not match confirm new password");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = IAM_PASSWORD_SET)
    @Override
    public void handle(Session session, SetPasswordMessage message) throws WriteResponseException {
        final Account account = authenticationContextProvider.get().getAccount();

        accountService.setAccountPassword(account.getId(), message.getNewPassword())
            .doOnEach(log.reactiveErrorThrowable("Error occurred while setting password"))
            .doOnEach(ReactiveTransaction.linkOnNext())
            .doOnEach(ReactiveTransaction.expireOnComplete())
            .subscriberContext(ReactiveMonitoring.createContext())
            .subscribe(ignore -> {
                   // nothing here, never executed
                }, ex -> {
                   log.jsonDebug("Unable to set password", new HashMap<String, Object>(){
                       {
                           put("message", message.toString());
                           put("error", ex.getStackTrace());
                       }
                   });
                   Responses.errorReactive(session, message.getId(), IAM_PASSWORD_SET_ERROR, HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                           "Unable to set password");
                },
                ()-> Responses.writeReactive(session, new BasicResponseMessage(IAM_PASSWORD_SET_OK, message.getId())));
    }
}
