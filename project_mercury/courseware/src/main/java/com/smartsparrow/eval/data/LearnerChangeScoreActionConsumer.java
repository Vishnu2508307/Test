package com.smartsparrow.eval.data;

import java.util.List;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.eval.action.progress.EmptyActionResult;
import com.smartsparrow.eval.action.score.ChangeScoreAction;
import com.smartsparrow.exception.IllegalStateFault;
import com.smartsparrow.learner.service.ChangeScoreEventHandler;
import com.smartsparrow.learner.service.StudentScoreService;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

/**
 * Change score action consumer
 * TODO add unit tests, logic copied from {@link ChangeScoreEventHandler}
 */
public class LearnerChangeScoreActionConsumer implements ActionConsumer<ChangeScoreAction, EmptyActionResult> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(LearnerChangeScoreActionConsumer.class);

    private final ActionConsumerOptions options;
    private final StudentScoreService studentScoreService;

    @Inject
    public LearnerChangeScoreActionConsumer(final StudentScoreService studentScoreService) {
        this.studentScoreService = studentScoreService;
        this.options = new ActionConsumerOptions()
                .setAsync(false);
    }

    @Trace(async = true)
    @Override
    public Mono<EmptyActionResult> consume(ChangeScoreAction changeScoreAction, LearnerEvaluationResponseContext context) {
        // preparing required variables

        final List<CoursewareElement> ancestry = context.getAncestry();

        if (ancestry.isEmpty()) {
            // this is not possible! at least 1 element is always present in the ancestry
            throw new IllegalStateFault("error processing CHANGE_SCORE action. Ancestry cannot be empty");
        }

        // persist the score entry for this walkable
        return studentScoreService.create(changeScoreAction, context)
                .doOnEach(ReactiveTransaction.linkOnNext())
                // roll the score up the courseware structure, omit the evaluated element in the ancestry
                .flatMapMany(persisted -> studentScoreService.rollUpScoreEntries(persisted, ancestry.subList(1, ancestry.size())))
                .doOnError(throwable -> {
                    if (log.isErrorEnabled()) {
                        log.error("error persisting score entries", throwable);
                    }
                })
                // collect to an ordered list of persisted entries
                .collectList()
                // CHANGE_SCORE action
                .map(scoreEntries -> new EmptyActionResult(changeScoreAction));
    }

    @Override
    public Mono<ActionConsumerOptions> getActionConsumerOptions() {
        return Mono.just(options);
    }
}
