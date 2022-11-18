package com.smartsparrow.rtm.message.handler.iam;

import javax.inject.Inject;

import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.EmptyReceivedMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.iam.IamAccountProvisionRTMSubscription;
import com.smartsparrow.rtm.subscription.iam.IamAccountProvisionRTMSubscription.IamAccountProvisionRTMSubscriptionFactory;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class AccountProvisionSubscribeHandler implements MessageHandler<EmptyReceivedMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AccountProvisionSubscribeHandler.class);

    public static final String IAM_ACCOUNT_PROVISION_SUBSCRIBE = "iam.account.provision.subscribe";
    private static final String IAM_ACCOUNT_PROVISION_SUBSCRIBE_OK = "iam.account.provision.subscribe.ok";
    private static final String IAM_ACCOUNT_PROVISION_SUBSCRIBE_ERROR = "iam.account.provision.subscribe.error";
    public static final String IAM_ACCOUNT_PROVISION_SUBSCRIBE_BROADCAST = "iam.account.provision.broadcast";
    public static final String IAM_ACCOUNT_PROVISION_SUBSCRIBE_BROADCAST_ERROR = "iam.account.provision.broadcast.error";

    private final Provider<RTMSubscriptionManager> subscriptionManagerProvider;
    private final Provider<AuthenticationContext> authenticationContextProvider;
    private final IamAccountProvisionRTMSubscriptionFactory iamAccountProvisionRTMSubscriptionFactory;

    @Inject
    public AccountProvisionSubscribeHandler(Provider<RTMSubscriptionManager> subscriptionManagerProvider,
                                            IamAccountProvisionRTMSubscriptionFactory iamAccountProvisionRTMSubscriptionFactory,
                                            Provider<AuthenticationContext> authenticationContextProvider) {
        this.subscriptionManagerProvider = subscriptionManagerProvider;
        this.iamAccountProvisionRTMSubscriptionFactory = iamAccountProvisionRTMSubscriptionFactory;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @SuppressWarnings("Duplicates")
    @Override
    @Trace(dispatcher = true, nameTransaction = false, metricName = IAM_ACCOUNT_PROVISION_SUBSCRIBE)
    public void handle(final Session session, final EmptyReceivedMessage message) throws WriteResponseException {
        final String messageId = message.getId();
        Account account = authenticationContextProvider.get().getAccount();
        IamAccountProvisionRTMSubscription rtmSubscription = iamAccountProvisionRTMSubscriptionFactory.create(account.getSubscriptionId());

        //Add subscriber to manager
        subscriptionManagerProvider.get()
                .add(rtmSubscription)
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(
                        listenerId -> {
                        },
                        this::subscriptionOnErrorHandler,
                        () -> {
                            BasicResponseMessage responseMessage = new BasicResponseMessage(
                                    IAM_ACCOUNT_PROVISION_SUBSCRIBE_OK,
                                    messageId);
                            responseMessage.addField("rtmSubscriptionId", rtmSubscription.getId());
                            Responses.writeReactive(session, responseMessage);
                        }
                );
    }

}
