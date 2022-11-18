package com.smartsparrow.rtm.message.handler.iam;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.EmptyReceivedMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;

/**
 * Returns list of all accounts with workspace roles of an subscription, excluding current authenticated user.
 */
public class ListUserMessageHandler implements MessageHandler<EmptyReceivedMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ListUserMessageHandler.class);
    public static final String IAM_SUBSCRIPTION_USER_LIST = "iam.subscription.user.list";
    public static final String IAM_SUBSCRIPTION_USER_LIST_OK = "iam.subscription.user.list.ok";

    private final Provider<AuthenticationContext> authenticationContextProvider;
    private final AccountService accountService;

    @Inject
    public ListUserMessageHandler(Provider<AuthenticationContext> authenticationContextProvider,
                                  AccountService accountService) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.accountService = accountService;
    }

    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = IAM_SUBSCRIPTION_USER_LIST)
    public void handle(Session session, EmptyReceivedMessage message) throws WriteResponseException {

        Account currentAccount = authenticationContextProvider.get().getAccount();

        Flux<Account> accounts =
                accountService.findBySubscription(currentAccount.getSubscriptionId())
                        .doOnEach(log.reactiveErrorThrowable("exception while fetching accounts for a subscriptionId"))
                        // link each signal to the current transaction token
                        .doOnEach(ReactiveTransaction.linkOnNext())
                        // expire the transaction token on completion
                        .doOnEach(ReactiveTransaction.expireOnComplete())
                        // create a reactive context that enables all supported reactive monitoring
                        .subscriberContext(ReactiveMonitoring.createContext())
                        .filter(a -> a.getRoles().stream().anyMatch(AccountRole.WORKSPACE_ROLES::contains));

        List<AccountPayload> response =
                accounts.flatMap(accountService::getAccountPayload)
                        .doOnEach(log.reactiveDebugSignal("account info of the user", account -> new HashMap<String, Object>(){
                    {
                        put("accountId", account.getAccountId());
                        put("subscriptionId", account.getSubscriptionId());
                        put("region", account.getIamRegion());
                    }
                }))
                        .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                        .collectList().block();

        BasicResponseMessage basicResponseMessage = new BasicResponseMessage(IAM_SUBSCRIPTION_USER_LIST_OK, message.getId());
        basicResponseMessage.addField("accounts", response);
        Responses.write(session, basicResponseMessage);

    }
}
