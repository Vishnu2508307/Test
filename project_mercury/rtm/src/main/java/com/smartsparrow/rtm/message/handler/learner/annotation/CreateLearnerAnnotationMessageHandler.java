package com.smartsparrow.rtm.message.handler.learner.annotation;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.learner.annotation.CreateLearnerAnnotationMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import javax.inject.Inject;
import java.util.HashMap;

import static com.smartsparrow.util.Warrants.affirmArgument;

public class CreateLearnerAnnotationMessageHandler implements MessageHandler<CreateLearnerAnnotationMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CreateLearnerAnnotationMessageHandler.class);

    public static final String LEARNER_ANNOTATION_CREATE = "learner.annotation.create";
    public static final String LEARNER_ANNOTATION_CREATE_OK = "learner.annotation.create.ok";
    public static final String LEARNER_ANNOTATION_CREATE_ERROR = "learner.annotation.create.error";

    private final AnnotationService annotationService;
    private final AuthenticationContextProvider authenticationContextProvider;

    @Inject
    public CreateLearnerAnnotationMessageHandler(final AnnotationService annotationService,
                                                 final AuthenticationContextProvider authenticationContextProvider) {
        this.annotationService = annotationService;
        this.authenticationContextProvider = authenticationContextProvider;
    }

    @Override
    public void validate(CreateLearnerAnnotationMessage message) throws RTMValidationException {
        affirmArgument(message.getDeploymentId() != null, "missing deploymentId");
        affirmArgument(message.getElementId() != null, "missing elementId");
        affirmArgument(message.getElementType() != null, "missing elementType");
        affirmArgument(message.getMotivation() != null, "missing motivation");
        affirmArgument(message.getBody() != null, "missing body");
        affirmArgument(message.getTarget() != null, "missing target");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = LEARNER_ANNOTATION_CREATE)
    @Override
    public void handle(Session session, CreateLearnerAnnotationMessage message) throws WriteResponseException {
        final Account account = authenticationContextProvider.get().getAccount();

                annotationService.create(message.getDeploymentId(),
                        message.getElementId(),
                        message.getBody(),
                        message.getTarget(),
                        account.getId(),
                        message.getMotivation())
                        .doOnEach(log.reactiveErrorThrowable("error creating learner annotation", throwable -> new HashMap<String, Object>() {
                            {
                                put("deploymentId", message.getDeploymentId());
                            }
                        }))
                        .doOnEach(ReactiveTransaction.linkOnNext())
                        .doOnEach(ReactiveTransaction.expireOnComplete())
                        .subscriberContext(ReactiveMonitoring.createContext())
                        .subscribe(learnerAnnotation -> {
                                    BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                            LEARNER_ANNOTATION_CREATE_OK,
                                            message.getId());
                                    basicResponseMessage.addField("learnerAnnotation", learnerAnnotation);
                                    Responses.writeReactive(session, basicResponseMessage);
                                },
                                ex -> {
                                    log.jsonDebug("Unable to create the learner annotation", new HashMap<String, Object>() {
                                        {
                                            put("message", message.toString());
                                            put("error", ex.getStackTrace());
                                        }
                                    });
                                    Responses.errorReactive(session, message.getId(), LEARNER_ANNOTATION_CREATE_ERROR, HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                            "Unable to create the learner annotation");
                                }
                        );

    }
}
