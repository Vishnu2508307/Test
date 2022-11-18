package com.smartsparrow.rtm.message.handler.iam;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.iam.SetAccountPasswordMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class SetAccountPasswordMessageHandler implements MessageHandler<SetAccountPasswordMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(SetAccountPasswordMessageHandler.class);

    public static final String IAM_ACCOUNT_PASSWORD_SET = "iam.account.password.set";
    public static final String IAM_ACCOUNT_PASSWORD_SET_OK = "iam.account.password.set.ok";
    public static final String IAM_ACCOUNT_PASSWORD_SET_ERROR = "iam.account.password.set.error";

    private final AccountService accountService;

    @Inject
    SetAccountPasswordMessageHandler(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void validate(SetAccountPasswordMessage message) throws RTMValidationException {
        affirmArgument(message.getAccountId() != null, "missing accountId");
        affirmArgument(message.getPassword() != null, "missing password");
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = IAM_ACCOUNT_PASSWORD_SET)
    public void handle(Session session, SetAccountPasswordMessage message) throws WriteResponseException {
        accountService.setAccountPassword(message.getAccountId(), message.getPassword())
            .doOnEach(log.reactiveErrorThrowable("Error occurred while setting account password"))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
            .subscribe(ignore -> {
                   // nothing here, never executed
                }, ex -> {
                   log.jsonDebug("Unable to set account password", new HashMap<String, Object>(){
                       {
                           put("message", message.toString());
                           put("error", ex.getStackTrace());
                       }
                   });
                   Responses.errorReactive(session, message.getId(), IAM_ACCOUNT_PASSWORD_SET_ERROR, HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                           "Unable to set account password");
                },
                ()-> Responses.writeReactive(session, new BasicResponseMessage(IAM_ACCOUNT_PASSWORD_SET_OK, message.getId())));
    }
}
