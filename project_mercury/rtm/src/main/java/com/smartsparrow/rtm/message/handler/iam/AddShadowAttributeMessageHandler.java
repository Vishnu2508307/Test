package com.smartsparrow.rtm.message.handler.iam;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AccountShadowAttributeSource;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.iam.AccountShadowAttributeMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class AddShadowAttributeMessageHandler implements MessageHandler<AccountShadowAttributeMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AuthenticateMessageHandler.class);

    public static final String IAM_SHADOW_ATTRIBUTE_ADD = "iam.shadow.attribute.add";
    public static final String IAM_SHADOW_ATTRIBUTE_ADD_OK = "iam.shadow.attribute.add.ok";
    public static final String IAM_SHADOW_ATTRIBUTE_ADD_ERROR = "iam.shadow.attribute.add.error";

    private final AccountService accountService;

    @Inject
    public AddShadowAttributeMessageHandler(final AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void validate(final AccountShadowAttributeMessage message) throws RTMValidationException {

        affirmArgument(message.getAccountId() != null, "missing accountId");
        affirmArgument(message.getValue() != null, "missing value");
        affirmArgument(message.getAccountShadowAttributeName() != null, "missing Shadow Name");
        try {
            if (message.getAccountId() != null) {
                Account account = accountService.findById(message.getAccountId()).blockFirst();
                checkArgument(account != null, String.format("account %s not found", message.getAccountId()));
            }
        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), IAM_SHADOW_ATTRIBUTE_ADD_ERROR);
        }
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = IAM_SHADOW_ATTRIBUTE_ADD)
    public void handle(final Session session, final AccountShadowAttributeMessage message) throws WriteResponseException {
        accountService.findById(message.getAccountId())
                .flatMap(account -> {
                    return accountService.addShadowAttribute(account.getId(),
                                                             message.getAccountShadowAttributeName(),
                                                             message.getValue(),
                                                             AccountShadowAttributeSource.SYSTEM);
                })
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(summary -> {
                  //Do nothing
                }, ex -> {
                    log.jsonDebug("error creating shadow attribute for account ", new HashMap<String, Object>() {
                        {
                            put("accountId", message.getAccountId());
                            put("id", message.getId());
                            put("error", ex.getStackTrace());
                        }
                    });

                    Responses.errorReactive(session, message.getId(), IAM_SHADOW_ATTRIBUTE_ADD_ERROR,
                                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "creating account shadow");
                }, () -> {
                    Responses.writeReactive(session, new BasicResponseMessage(IAM_SHADOW_ATTRIBUTE_ADD_OK, message.getId()));
                });
    }

    @Override
    public void subscriptionOnErrorHandler(final Throwable exception) {
        MessageHandler.super.subscriptionOnErrorHandler(exception);
    }
}
