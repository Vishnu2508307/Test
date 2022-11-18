package com.smartsparrow.rtm.message.handler.learner.annotation;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.learner.annotation.UpdateLearnerAnnotationMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class UpdateLearnerAnnotationMessageHandler implements MessageHandler<UpdateLearnerAnnotationMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(UpdateLearnerAnnotationMessageHandler.class);

    public static final String LEARNER_ANNOTATION_UPDATE = "learner.annotation.update";
    public static final String LEARNER_ANNOTATION_UPDATE_OK = "learner.annotation.update.ok";
    public static final String LEARNER_ANNOTATION_UPDATE_ERROR = "learner.annotation.update.error";

    private final AnnotationService annotationService;

    @Inject
    public UpdateLearnerAnnotationMessageHandler(final AnnotationService annotationService) {
        this.annotationService = annotationService;
    }

    @Override
    public void validate(UpdateLearnerAnnotationMessage message) throws RTMValidationException {
        affirmArgument(message.getDeploymentId() != null, "missing deploymentId");
        affirmArgument(message.getElementId() != null, "missing elementId");
        affirmArgument(message.getElementType() != null, "missing elementType");
        affirmArgument(message.getAnnotationId() != null, "missing annotation id");
        affirmArgument(message.getBody() != null, "missing annotation body");
        affirmArgument(message.getMotivation() != null, "missing motivation");
        affirmArgument(message.getTarget() != null, "missing annotation target");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = LEARNER_ANNOTATION_UPDATE)
    @Override
    public void handle(Session session, UpdateLearnerAnnotationMessage message) throws WriteResponseException {

        annotationService.updateLearnerAnnotation(message.getAnnotationId(), message.getBody(), message.getTarget())
            .doOnEach(log.reactiveErrorThrowable("error updating learner annotation", throwable -> new HashMap<String, Object>() {
                {
                    put("annotationId", message.getAnnotationId());
                }
            }))
            .doOnEach(ReactiveTransaction.linkOnNext())
            .doOnEach(ReactiveTransaction.expireOnComplete())
            .subscriberContext(ReactiveMonitoring.createContext())
            .subscribe(learnerAnnotation -> {
                    BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                        LEARNER_ANNOTATION_UPDATE_OK,
                        message.getId());
                    basicResponseMessage.addField("learnerAnnotation", learnerAnnotation);
                    Responses.writeReactive(session, basicResponseMessage);
                },
                ex -> {
                    log.jsonDebug("Unable to update the learner annotation", new HashMap<String, Object>() {
                        {
                            put("message", message.toString());
                            put("error", ex.getStackTrace());
                        }
                    });
                    Responses.errorReactive(session, message.getId(), LEARNER_ANNOTATION_UPDATE_ERROR, HttpStatus.SC_UNPROCESSABLE_ENTITY,
                        "Unable to update learner annotation");
                }
            );
    }
}
