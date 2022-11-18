package com.smartsparrow.learner.service;

import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.lang.ProgressNotFoundFault;
import com.smartsparrow.learner.progress.Progress;

import reactor.core.publisher.Mono;

@Singleton
public class BKTPathwayAttemptResolver implements PathwayAttemptResolver {

    private final AttemptService attemptService;
    private final ProgressService progressService;

    @Inject
    public BKTPathwayAttemptResolver(final AttemptService attemptService, final ProgressService progressService) {
        this.attemptService = attemptService;
        this.progressService = progressService;
    }

    @Override
    public Mono<Attempt> resolveInteractiveAttempt(final UUID deploymentId, final UUID interactiveId,
                                                   final UUID studentId, final Attempt parentPathwayAttempt,
                                                   final Attempt interactiveAttempt) {
        Mono<Progress> latestProgressMono = progressService.findLatest(deploymentId, interactiveId, studentId);

        return latestProgressMono
                .filter(latestProgress -> latestProgress.getAttemptId().equals(interactiveAttempt.getId()))
                .filter(latestProgress -> !latestProgress.getCompletion().isCompleted())
                .flatMap(latestProgress -> {
                    // The interaction's latest attempt has been evaluated against, and the progress is not completed so create a new attempt!
                    // associate to parent's id; increment latest value by 1.
                    return attemptService.newAttempt(deploymentId, studentId, INTERACTIVE, interactiveId, parentPathwayAttempt.getId(),
                    interactiveAttempt.getValue() + 1);
                })
                // there is no progress for the current attempt
                .switchIfEmpty(Mono.just(interactiveAttempt))
                // if the latest progress was not found return the current attempt
                .onErrorResume(ProgressNotFoundFault.class, ex -> Mono.just(interactiveAttempt));
    }
}
