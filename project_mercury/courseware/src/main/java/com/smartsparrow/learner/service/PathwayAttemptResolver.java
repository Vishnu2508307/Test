package com.smartsparrow.learner.service;

import java.util.UUID;

import com.smartsparrow.learner.attempt.Attempt;

import reactor.core.publisher.Mono;

interface PathwayAttemptResolver {

    Mono<Attempt> resolveInteractiveAttempt(final UUID deploymentId, final UUID interactiveId, final UUID studentId,
                                                     final Attempt parentPathwayAttempt, final Attempt interactiveAttempt);

}
