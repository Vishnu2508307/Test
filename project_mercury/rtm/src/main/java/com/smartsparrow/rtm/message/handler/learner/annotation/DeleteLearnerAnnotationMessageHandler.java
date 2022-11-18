package com.smartsparrow.rtm.message.handler.learner.annotation;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.learner.annotation.DeleteLearnerAnnotationMessage;
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

public class DeleteLearnerAnnotationMessageHandler implements MessageHandler<DeleteLearnerAnnotationMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(DeleteLearnerAnnotationMessageHandler.class);

    public static final String LEARNER_ANNOTATION_DELETE = "learner.annotation.delete";
    public static final String LEARNER_ANNOTATION_DELETE_OK = "learner.annotation.delete.ok";
    public static final String LEARNER_ANNOTATION_DELETE_ERROR = "learner.annotation.delete.error";

    private final AnnotationService annotationService;

    @Inject
    public DeleteLearnerAnnotationMessageHandler(final AnnotationService annotationService) {
        this.annotationService = annotationService;
    }

    @Override
    public void validate(DeleteLearnerAnnotationMessage message) throws RTMValidationException {
        affirmArgument(message.getAnnotationId() != null, "missing annotationId");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = LEARNER_ANNOTATION_DELETE)
    @Override
    public void handle(Session session, DeleteLearnerAnnotationMessage message) throws WriteResponseException {

        annotationService.findLearnerAnnotation(message.getAnnotationId())
                .flatMap(learnerAnnotation -> annotationService.deleteAnnotation(learnerAnnotation)
                        .singleOrEmpty()
                        .thenReturn(learnerAnnotation))
                .doOnEach(log.reactiveErrorThrowable("error deleting learner annotation", throwable -> new HashMap<String, Object>() {
                    {
                        put("annotationId", message.getAnnotationId());
                    }
                }))
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(ignore -> {
                            // nothing here, never executed
                        }, ex -> {
                            log.jsonDebug("Unable to delete learner annotation", new HashMap<String, Object>(){
                                {
                                    put("message", message.toString());
                                    put("error", ex.getStackTrace());
                                }
                            });
                            Responses.errorReactive(session, message.getId(), LEARNER_ANNOTATION_DELETE_ERROR,
                                    HttpStatus.SC_UNPROCESSABLE_ENTITY, "error deleting learner annotation");
                        },
                        ()-> Responses.writeReactive(session, new BasicResponseMessage(LEARNER_ANNOTATION_DELETE_OK, message.getId())));

    }
}
