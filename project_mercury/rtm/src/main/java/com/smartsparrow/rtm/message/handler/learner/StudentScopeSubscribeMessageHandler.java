package com.smartsparrow.rtm.message.handler.learner;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.learner.StudentScopeSubscribeMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.SubscriptionAlreadyExists;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.learner.studentscope.StudentScopeRTMSubscription;
import com.smartsparrow.rtm.util.NewRelic;
import com.smartsparrow.rtm.util.NewRelicTransactionAttributes;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.Exceptions;

public class StudentScopeSubscribeMessageHandler implements MessageHandler<StudentScopeSubscribeMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(StudentScopeSubscribeMessageHandler.class);

    public static final String LEARNER_STUDENT_SCOPE_SUBSCRIBE = "learner.student.scope.subscribe";
    public static final String LEARNER_STUDENT_SCOPE_SUBSCRIBE_OK = "learner.student.scope.subscribe.ok";
    public static final String LEARNER_STUDENT_SCOPE_SUBSCRIBE_ERROR = "learner.student.scope.subscribe.error";

    private final AuthenticationContextProvider authenticationContextProvider;
    private final Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;
    private final StudentScopeRTMSubscription.StudentScopeRTMSubscriptionFactory studentScopeRTMSubscriptionFactory;

    @Inject
    public StudentScopeSubscribeMessageHandler(AuthenticationContextProvider authenticationContextProvider,
                                               final Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider,
                                               final StudentScopeRTMSubscription.StudentScopeRTMSubscriptionFactory studentScopeRTMSubscriptionFactory) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.rtmSubscriptionManagerProvider = rtmSubscriptionManagerProvider;
        this.studentScopeRTMSubscriptionFactory = studentScopeRTMSubscriptionFactory;
    }

    @Override
    public void validate(StudentScopeSubscribeMessage message) throws RTMValidationException {
        affirmArgument(message.getDeploymentId() != null, "deploymentId is required");
    }

    @SuppressWarnings("Duplicates")
    @Trace(dispatcher = true, nameTransaction = false, metricName = LEARNER_STUDENT_SCOPE_SUBSCRIBE)
    @Override
    public void handle(Session session, StudentScopeSubscribeMessage message) throws WriteResponseException {
        final Account account = authenticationContextProvider.get().getAccount();
        NewRelic.addCustomAttribute(NewRelicTransactionAttributes.ACCOUNT_ID.getValue(), account.getId().toString(), log);

        StudentScopeRTMSubscription studentScopeRTMSubscription = studentScopeRTMSubscriptionFactory
                .create(account.getId(), message.getDeploymentId());

        rtmSubscriptionManagerProvider.get().add(studentScopeRTMSubscription)
                .doOnEach(log.reactiveErrorThrowable("exception occurred while adding the subscription"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(listenerId -> { },
                           ex -> {
                            Throwable unwrap = Exceptions.unwrap(ex);
                            log.jsonDebug("error occurred while adding the subscription", new HashMap<String, Object>() {
                                {
                                    put("id", message.getId());
                                    put("deploymentId", message.getDeploymentId());
                                    put("coursewareElementId", unwrap.getStackTrace());
                                }
                            });

                            // FIXME: PLT-5390, transient state to avoid flooding FE with subscription errors while they catch up
                            // to the change in scope subscriptions
                            if(unwrap instanceof SubscriptionAlreadyExists) {
                                BasicResponseMessage response = new BasicResponseMessage(LEARNER_STUDENT_SCOPE_SUBSCRIBE_OK, message.getId());
                                response.addField("rtmSubscriptionId", studentScopeRTMSubscription.getId());
                                Responses.writeReactive(session, response);

                            } else {
                                subscriptionOnErrorHandler(ex);
                            }
                            },
                            () -> {
                                BasicResponseMessage response = new BasicResponseMessage(LEARNER_STUDENT_SCOPE_SUBSCRIBE_OK, message.getId());
                                response.addField("rtmSubscriptionId", studentScopeRTMSubscription.getId());
                                Responses.writeReactive(session, response);
                            });
    }

}
