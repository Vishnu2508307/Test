package com.smartsparrow.learner.service;

import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.lang.ProgressNotFoundFault;
import com.smartsparrow.learner.progress.Progress;

import reactor.core.publisher.Mono;

@Singleton
public class GraphPathwayAttemptResolver implements PathwayAttemptResolver {

    private final AttemptService attemptService;
    private final ProgressService progressService;

    @Inject
    public GraphPathwayAttemptResolver(AttemptService attemptService,
                                       ProgressService progressService) {
        this.attemptService = attemptService;
        this.progressService = progressService;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public Mono<Attempt> resolveInteractiveAttempt(UUID deploymentId, UUID interactiveId, UUID studentId,
                                                   @Nonnull Attempt parentPathwayAttempt, @Nonnull Attempt interactiveAttempt) {

        Mono<Progress> latestProgressMono = progressService.findLatest(deploymentId, interactiveId, studentId);

        return latestProgressMono
                .flatMap(latestProgress -> {
                    if (latestProgress.getAttemptId().equals(interactiveAttempt.getId())) {
                        if (!latestProgress.getCompletion().isCompleted()) {
                            // The interaction's latest attempt has been evaluated against, and the progress is not completed so create a new attempt!
                            // associate to parent's id; increment latest value by 1.
                            return attemptService.newAttempt(deploymentId, studentId, INTERACTIVE, interactiveId, parentPathwayAttempt.getId(),
                                    interactiveAttempt.getValue() + 1);
                        }
                        // The interactive IS complete. restart back at 1; this occurs for interactives which loop onto themselves.
                        return attemptService.newAttempt(deploymentId, studentId, INTERACTIVE, interactiveId, parentPathwayAttempt.getId());
                    }

                    // there is no progress for the current attempt
                    return Mono.just(interactiveAttempt);
                })
                // if the latest progress was not found return the current attempt
                .onErrorResume(ProgressNotFoundFault.class, ex -> Mono.just(interactiveAttempt));
    }
}
