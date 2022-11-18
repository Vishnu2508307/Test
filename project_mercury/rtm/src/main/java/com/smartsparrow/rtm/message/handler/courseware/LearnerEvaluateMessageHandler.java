package com.smartsparrow.rtm.message.handler.courseware;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.config.ConfigurableFeatureValues;
import com.smartsparrow.eval.service.EvaluationServiceAdapter;
import com.smartsparrow.iam.service.AccountShadowAttributeName;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.lang.LearnerEvaluationException;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.courseware.LearnerEvaluateMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.Maps;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

public class LearnerEvaluateMessageHandler implements MessageHandler<LearnerEvaluateMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(LearnerEvaluateMessageHandler.class);

    public static final String LEARNER_EVALUATE = "learner.evaluate";

    private final EvaluationServiceAdapter evaluationServiceAdapter;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final AuthenticationContextProvider authenticationContextProvider;

    @Inject
    public LearnerEvaluateMessageHandler(final EvaluationServiceAdapter evaluationServiceAdapter,
                                         final Provider<RTMClientContext> rtmClientContextProvider,
                                         final AuthenticationContextProvider authenticationContextProvider) {
        this.evaluationServiceAdapter = evaluationServiceAdapter;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @Override
    public void validate(LearnerEvaluateMessage message) throws RTMValidationException {
        affirmArgument(message.getDeploymentId() != null, "deploymentId is required");
        affirmArgument(message.getInteractiveId() != null, "interactiveId is required");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = LEARNER_EVALUATE)
    @Override
    public void handle(Session session, LearnerEvaluateMessage message) throws WriteResponseException {

        final AuthenticationContext authenticationContext = authenticationContextProvider.get();
        final UUID studentId = authenticationContext.getAccount().getId();
        final AccountShadowAttributeName configuredEvaluation = authenticationContext.getAccountShadowAttribute(ConfigurableFeatureValues.EVALUATION);
        final String clientId = rtmClientContextProvider.get().getClientId();

        Mono<EvaluationResult> evaluationMono;

        if (message.getTimeId() != null) {
            evaluationMono = evaluationServiceAdapter.evaluate(message.getDeploymentId(), message.getInteractiveId(),
                                                               clientId, studentId, configuredEvaluation, message.getTimeId());
        } else {
            evaluationMono = evaluationServiceAdapter.evaluate(message.getDeploymentId(), message.getInteractiveId(), clientId, studentId, configuredEvaluation);
        }

        evaluationMono
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .doOnEach(log.reactiveErrorThrowable("Error evaluating"))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscribe(
                        evaluationResult -> {
                            BasicResponseMessage msg = new BasicResponseMessage(LEARNER_EVALUATE + ".ok",
                                    message.getId());
                            msg.addField("evaluationResult", evaluationResult);
                            Responses.writeReactive(session, msg);
                        },
                        ex -> {

                            if (ex instanceof LearnerEvaluationException) {
                                log.jsonDebug("unable to evaluate interactive", new HashMap<String, Object>() {
                                    {
                                        put("interactiveId", message.getInteractiveId());
                                        put("deploymentId", message.getDeploymentId());
                                        put("error", ex.getStackTrace());
                                    }
                                });
                                Responses.errorReactive(session, message.getId(), LEARNER_EVALUATE + ".error",
                                        HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                        "Unable to evaluate",
                                        Maps.ofObject("evaluationId", ((LearnerEvaluationException) ex).getEvaluationId()));
                            } else {
                                log.jsonDebug("unexpected error occurred", new HashMap<String, Object>() {
                                    {
                                        put("error", ex.getStackTrace());
                                    }
                                });
                                Responses.errorReactive(session, message.getId(), LEARNER_EVALUATE + ".error", ex);
                            }
                        }
                );
    }
}
