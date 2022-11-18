package com.smartsparrow.rtm.message.handler.learner.annotation;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.annotation.service.LearnerAnnotation;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.learner.ListLearnerAnnotationMessage;
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

import reactor.core.publisher.Flux;

/**
 * This message handler is deprecated and it will be removed soon
 */
@Deprecated
public class ListDeploymentAnnotationMessageHandler implements MessageHandler<ListLearnerAnnotationMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ListDeploymentAnnotationMessageHandler.class);

    public static final String LEARNER_ANNOTATION_LIST = "learner.annotation.list";
    public static final String LEARNER_ANNOTATION_LIST_OK = "learner.annotation.list.ok";
    public static final String LEARNER_ANNOTATION_LIST_ERROR = "learner.annotation.list.error";

    private final AnnotationService annotationService;

    @Inject
    public ListDeploymentAnnotationMessageHandler(final AnnotationService annotationService) {
        this.annotationService = annotationService;
    }

    @Override
    public void validate(ListLearnerAnnotationMessage message) throws RTMValidationException {
        affirmArgument(message.getDeploymentId() != null, "missing deploymentId");
        affirmArgument(message.getElementType() != null, "missing elementType");
        affirmArgument(message.getCreatorAccountId() != null, "missing creatorAccountId");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = LEARNER_ANNOTATION_LIST)
    @Override
    public void handle(Session session, ListLearnerAnnotationMessage message) throws WriteResponseException {
        Flux<LearnerAnnotation> learnerAnnotationFlux;
        if (message.getElementId() != null) {
            // find with an element id as a specifier
            learnerAnnotationFlux = annotationService.findLearnerAnnotation(message.getDeploymentId(),
                                                                            message.getCreatorAccountId(),
                                                                            message.getMotivation(),
                                                                            message.getElementId());
        } else {
            // find all within the deployment
            learnerAnnotationFlux = annotationService.findLearnerAnnotation(message.getDeploymentId(),
                                                                            message.getCreatorAccountId(),
                                                                            message.getMotivation());
        }

        learnerAnnotationFlux
                .doOnEach(log.reactiveErrorThrowable("error fetching deployment annotation", throwable -> new HashMap<String, Object>() {
                {
                    put("deploymentId", message.getDeploymentId());
                    put("motivation", message.getMotivation());
                }
            }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
            .collectList()
            .subscribe(learnerAnnotations -> {
                       BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                               LEARNER_ANNOTATION_LIST_OK,
                               message.getId());
                       basicResponseMessage.addField("learnerAnnotation", learnerAnnotations);
                       Responses.writeReactive(session, basicResponseMessage);
                   },
                   ex -> {
                       log.jsonDebug("Unable to fetch the deployment annotation", new HashMap<String, Object>() {
                           {
                               put("message", message.toString());
                               put("error", ex.getStackTrace());
                           }
                       });
                       Responses.errorReactive(session, message.getId(), LEARNER_ANNOTATION_LIST_ERROR, HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                               "Unable to fetch deployment annotation");
                   }
            );
    }
}
