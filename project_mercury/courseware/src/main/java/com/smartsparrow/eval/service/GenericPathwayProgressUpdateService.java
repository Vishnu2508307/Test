package com.smartsparrow.eval.service;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.courseware.pathway.PathwayType;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.data.LearnerEvaluationResponseContext;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.learner.service.LearnerPathwayService;

import reactor.core.publisher.Mono;

/**
 * Implements the pathway progress update logic. This class acts as a proxy that routes the progress update to the
 * proper implementation based on the pathway type
 */
@Singleton
public class GenericPathwayProgressUpdateService implements ProgressUpdateService {

    private final Map<PathwayType, Provider<PathwayProgressUpdateService<? extends LearnerPathway>>> pathwayProgressUpdateServiceImplProvider;
    private final LearnerPathwayService learnerPathwayService;

    @Inject
    public GenericPathwayProgressUpdateService(final Map<PathwayType, Provider<PathwayProgressUpdateService<? extends LearnerPathway>>> pathwayProgressUpdateServiceImplProvider,
                                               final LearnerPathwayService learnerPathwayService) {
        this.pathwayProgressUpdateServiceImplProvider = pathwayProgressUpdateServiceImplProvider;
        this.learnerPathwayService = learnerPathwayService;
    }

    /**
     * Find the specific pathway progress service implementation and invokes it returing its result
     *
     * @param element the element to update the progress for
     * @param action the action that triggered the progress update
     * @param responseContext the context holding evaluation information that triggered the action consumer in the first
     *                        place
     * @return a mono including the pathway progress
     */
    @Trace(async = true)
    @SuppressWarnings("unchecked")
    @Override
    public Mono<Progress> updateProgress(CoursewareElement element, ProgressAction action, LearnerEvaluationResponseContext responseContext) {
        // first we need to find the pathway
        return Mono.just(responseContext)
                .flatMap(context -> {
                    // if this pathway is the direct parent of the evaluated element we might already have it in the context,
                    // check there first so we can spare a query
                    LearnerPathway parentPathway = responseContext.getParentPathway();
                    if (parentPathway != null && parentPathway.getId().equals(element.getElementId())) {
                        return Mono.just(parentPathway);
                    }
                    // otherwise just query the learner pathway
                    return learnerPathwayService.find(element.getElementId(), responseContext.getResponse().getEvaluationRequest().getDeployment().getId());
                })
                // we have the pathway, call the bound implementation supplying the pathway
                .flatMap(learnerPathway -> ((PathwayProgressUpdateService<LearnerPathway>) pathwayProgressUpdateServiceImplProvider.get(learnerPathway.getType())
                        .get())
                        .updateProgress(learnerPathway, action, responseContext));
    }
}
