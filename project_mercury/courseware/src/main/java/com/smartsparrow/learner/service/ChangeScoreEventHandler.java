package com.smartsparrow.learner.service;

import static com.smartsparrow.courseware.route.CoursewareRoutes.EVALUATION_EVENT_MESSAGE;

import java.util.List;

import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.eval.action.score.ChangeScoreAction;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.learner.event.EvaluationEventMessage;

public class ChangeScoreEventHandler {

    private static final Logger log = LoggerFactory.getLogger(ChangeScoreEventHandler.class);

    private final StudentScoreService studentScoreService;

    @Inject
    public ChangeScoreEventHandler(StudentScoreService studentScoreService) {
        this.studentScoreService = studentScoreService;
    }

    @Handler
    public void handle(Exchange exchange) {

        // preparing required variables
        final EvaluationEventMessage eventMessage = exchange.getProperty(EVALUATION_EVENT_MESSAGE, EvaluationEventMessage.class);
        final ChangeScoreAction action = exchange.getIn().getBody(ChangeScoreAction.class);
        final List<CoursewareElement> ancestry = eventMessage.getAncestryList();

        if (ancestry.isEmpty()) {
            // this is not possile! at least 1 element is always present in the ancestry
            throw new IllegalStateFault("error processing CHANGE_SCORE action. Ancestry cannot be empty");
        }

        if (log.isDebugEnabled()) {
            log.debug("processing change score action: {} for student {}", action, eventMessage.getStudentId());
        }

        // persist the score entry for this screen
        studentScoreService.create(action, eventMessage)
                // roll the score up the courseware structure, omit the evaluated element in the ancestry
                .flatMapMany(persisted -> studentScoreService.rollUpScoreEntries(persisted, ancestry.subList(1, ancestry.size())))
                .doOnError(throwable -> {
                    if (log.isErrorEnabled()) {
                        log.error("error persisting score entries", throwable);
                    }
                })
                // collect to an ordered list of persisted entries
                .collectList()
                // block the call, this is important especially if the next handled action is another
                // CHANGE_SCORE action
                .block();

    }
}
