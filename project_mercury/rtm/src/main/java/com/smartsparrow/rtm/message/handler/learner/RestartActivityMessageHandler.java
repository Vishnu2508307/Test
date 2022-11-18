package com.smartsparrow.rtm.message.handler.learner;

import static com.smartsparrow.util.Warrants.affirmNotNull;

import java.util.UUID;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.rtm.util.NewRelic;
import com.smartsparrow.rtm.util.NewRelicTransactionAttributes;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.learner.service.RestartActivityService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.learner.RestartActivityMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;

public class RestartActivityMessageHandler implements MessageHandler<RestartActivityMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(RestartActivityMessageHandler.class);

    public static final String LEARNER_ACTIVITY_RESTART = "learner.activity.restart";
    public static final String LEARNER_ACTIVITY_RESTART_OK = "learner.activity.restart.ok";
    public static final String LEARNER_ACTIVITY_RESTART_ERROR = "learner.activity.restart.error";

    private final RestartActivityService restartActivityService;
    private final AuthenticationContextProvider authenticationContextProvider;

    @Inject
    public RestartActivityMessageHandler(RestartActivityService restartActivityService,
                                         AuthenticationContextProvider authenticationContextProvider) {
        this.restartActivityService = restartActivityService;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @Override
    public void validate(RestartActivityMessage message) throws RTMValidationException {
        affirmNotNull(message.getActivityId(), "activityId is required");
        affirmNotNull(message.getDeploymentId(), "deploymentId is required");
    }


    @Trace(dispatcher = true, nameTransaction = false, metricName = LEARNER_ACTIVITY_RESTART)
    @Override
    public void handle(Session session, RestartActivityMessage message) {
        UUID accountId = authenticationContextProvider.get().getAccount().getId();
        NewRelic.addCustomAttribute(NewRelicTransactionAttributes.ACCOUNT_ID.getValue(), accountId.toString().toString(), log);

        restartActivityService.restartActivity(message.getDeploymentId(), message.getActivityId(), accountId)
                .doOnEach(log.reactiveErrorThrowable("exception occurred while restarting the activity"))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscribe(progress -> {
                    BasicResponseMessage basicResponseMessage = new BasicResponseMessage(LEARNER_ACTIVITY_RESTART_OK, message.getId());
                    Responses.writeReactive(session, basicResponseMessage);
                }, ex -> Responses.errorReactive(session, message.getId(), LEARNER_ACTIVITY_RESTART_ERROR, ex));
    }
}
