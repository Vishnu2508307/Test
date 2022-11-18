package com.smartsparrow.eval.service;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.progress.ProgressAction;
import com.smartsparrow.eval.data.LearnerEvaluationResponseContext;
import com.smartsparrow.eval.data.WalkableEvaluationResult;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.service.LearnerCoursewareService;
import com.smartsparrow.learner.service.StudentScopeService;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Mono;

@Singleton
public class LearnerEvaluationResponseEnricher {

    private final LearnerScenarioActionEnricher learnerScenarioActionEnricher;
    private final LearnerCoursewareService learnerCoursewareService;
    private final StudentScopeService studentScopeService;

    @Inject
    public LearnerEvaluationResponseEnricher(final LearnerScenarioActionEnricher learnerScenarioActionEnricher,
                                             final LearnerCoursewareService learnerCoursewareService,
                                             final StudentScopeService studentScopeService) {
        this.learnerScenarioActionEnricher = learnerScenarioActionEnricher;
        this.learnerCoursewareService = learnerCoursewareService;
        this.studentScopeService = studentScopeService;
    }

    /**
     * Enriches a learner evaluation response. First calls the {@link LearnerScenarioActionEnricher} to make sure
     * actions are deserialized and resolved. Then computes whether the evaluated walkable should be marked as completed
     * or not.
     *
     * @param responseContext the response context to enrich
     * @return a mono with the enriched response context
     */
    @Trace(async = true)
    public Mono<LearnerEvaluationResponseContext> enrich(final LearnerEvaluationResponseContext responseContext) {
        affirmArgument(responseContext != null, "responseContext is required");

        // make sure the actions are deserialized and resolved
        return learnerScenarioActionEnricher.enrich(responseContext)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .map(enrichedResponseContext -> {
                    // get the walkable evaluation result from the action enriched response context
                    WalkableEvaluationResult result = enrichedResponseContext.getResponse()
                            .getWalkableEvaluationResult();
                    // find if the evaluated walkable should be marked as completed or not
                    boolean isWalkableCompleted = result
                            // get all the actions that were triggered in this evaluation
                            .getTriggeredActions()
                            .stream()
                            // only return the CHANGE_PROGRESS action type (it should always be there)
                            .filter(action -> action.getType().equals(Action.Type.CHANGE_PROGRESS))
                            // find if the progression action defines the walkable as completed
                            .anyMatch(action -> {
                                try {
                                    ProgressAction progressAction = (ProgressAction) action;
                                    // pass the current walkable to the method so we are sure to get the result
                                    // for the evaluated walkable
                                    return progressAction.isWalkableCompleted(result.getWalkableType());
                                } catch (ClassCastException e) {
                                    return false;
                                }
                            });
                    // mark whether the walkable is completed or not
                    result.setWalkableComplete(isWalkableCompleted);
                    // return the enriched response context
                    return enrichedResponseContext;
                })
                // finally add the ancestry to the context so it can be used to consume actions such as
                // change score or change progress
                .flatMap(enrichedResponseContext -> {
                    // get the walkable
                    final LearnerWalkable walkable = enrichedResponseContext.getResponse()
                            .getEvaluationRequest()
                            .getLearnerWalkable();
                    // find the ancestry
                    Mono<List<CoursewareElement>> ancestryMono = learnerCoursewareService
                            .getAncestry(walkable.getDeploymentId(), walkable.getId(), walkable.getElementType());
                    final UUID studentId = responseContext.getResponse().getEvaluationRequest().getStudentId();
                    // find the scope map
                    Mono<Map<UUID, String>> scopeEntriesMapMono = studentScopeService
                            .findLatestEntries(walkable.getDeploymentId(), studentId, walkable.getStudentScopeURN());

                    return Mono.zip(ancestryMono, scopeEntriesMapMono)
                            .map(tuple2 -> {
                                // set the ancestry to the context
                                enrichedResponseContext.setAncestry(tuple2.getT1());
                                // set the scope entries to the context
                                enrichedResponseContext.setScopeEntriesMap(tuple2.getT2());
                                // return the enriched context
                                return enrichedResponseContext;
                            });
                });
    }
}
