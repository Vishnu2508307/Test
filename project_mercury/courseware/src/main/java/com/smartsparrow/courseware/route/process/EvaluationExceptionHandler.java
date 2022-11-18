package com.smartsparrow.courseware.route.process;

import static com.smartsparrow.courseware.route.CoursewareRoutes.EVALUATION_EVENT_MESSAGE;

import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.eval.service.EvaluationErrorService;
import com.smartsparrow.learner.event.EvaluationEventMessage;
import com.smartsparrow.learner.lang.LearnerEvaluationException;

public class EvaluationExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(EvaluationExceptionHandler.class);

    private final EvaluationErrorService evaluationErrorService;

    @Inject
    public EvaluationExceptionHandler(final EvaluationErrorService evaluationErrorService) {
        this.evaluationErrorService = evaluationErrorService;
    }

    @Handler
    public void handle(Exchange exchange) {
        final EvaluationEventMessage eventMessage = exchange.getProperty(EVALUATION_EVENT_MESSAGE, EvaluationEventMessage.class);
        final Exception ex = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);

        if (log.isDebugEnabled()) {
            log.debug("preparing to persist evaluation exception {}", ex.getMessage());
        }

        // create an evaluation error
        evaluationErrorService.createGeneric(ex, eventMessage.getEvaluationId()).subscribe();

        // throw the exception including the evaluation id
        throw new LearnerEvaluationException("error evaluating", ex, eventMessage.getEvaluationId());
    }
}
