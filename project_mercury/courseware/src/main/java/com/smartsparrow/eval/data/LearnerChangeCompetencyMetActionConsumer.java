package com.smartsparrow.eval.data;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.competency.data.AssociationType;
import com.smartsparrow.competency.data.ItemAssociation;
import com.smartsparrow.eval.action.competency.ChangeCompetencyMetAction;
import com.smartsparrow.eval.action.competency.ChangeCompetencyMetActionContext;
import com.smartsparrow.eval.action.progress.EmptyActionResult;
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.learner.data.CompetencyMet;
import com.smartsparrow.learner.data.CompetencyMetByStudent;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.service.ChangeCompetencyMetEventHandler;
import com.smartsparrow.learner.service.CompetencyMetService;
import com.smartsparrow.learner.service.LearnerCompetencyDocumentService;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * This class consumes change competency met actions. Logic was taken from {@link ChangeCompetencyMetEventHandler}
 */
public class LearnerChangeCompetencyMetActionConsumer implements ActionConsumer<ChangeCompetencyMetAction, EmptyActionResult> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(LearnerChangeCompetencyMetActionConsumer.class);

    private final ActionConsumerOptions options;
    private final CompetencyMetService competencyMetService;
    private final LearnerCompetencyDocumentService learnerCompetencyDocumentService;

    @Inject
    public LearnerChangeCompetencyMetActionConsumer(final CompetencyMetService competencyMetService,
                                                    final LearnerCompetencyDocumentService learnerCompetencyDocumentService) {
        this.options = new ActionConsumerOptions()
                .setAsync(false);
        this.competencyMetService = competencyMetService;
        this.learnerCompetencyDocumentService = learnerCompetencyDocumentService;
    }

    @Trace(async = true)
    @Override
    public Mono<EmptyActionResult> consume(ChangeCompetencyMetAction changeCompetencyMetAction, LearnerEvaluationResponseContext responseContext) {
        // prepare variables
        final LearnerEvaluationResponse response = responseContext.getResponse();
        final LearnerEvaluationRequest request = response.getEvaluationRequest();
        final Deployment deployment = request.getDeployment();
        final LearnerWalkable learnerWalkable = request.getLearnerWalkable();
        final ChangeCompetencyMetActionContext actionContext = changeCompetencyMetAction.getContext();
        final UUID documentId = actionContext.getDocumentId();
        final UUID documentItemId = actionContext.getDocumentItemId();
        final UUID studentId = request.getStudentId();

        // find the document cause we need the version
        // (future improvement save the version to the action context so we can spare this query)
        return learnerCompetencyDocumentService.findDocument(documentId)
                .doOnEach(ReactiveTransaction.linkOnNext())
                // Calculate new competency met awarded value
                .flatMap(document -> {
                    Float value = actionContext.getValue();

                    return Mono.just(value)
                            .flatMap(contextValue -> {
                                // find out what the opartor is
                                switch (actionContext.getOperator()) {
                                    // set is easy this value will override
                                    case SET:
                                        return Mono.just(contextValue);
                                    case ADD:
                                    case REMOVE:
                                        // need tp find latest competency met to check for already existing value, or default to 0F if none found
                                        return competencyMetService.findLatest(studentId, documentId, documentItemId)
                                                .defaultIfEmpty(new CompetencyMetByStudent().setValue(0f))
                                                .map(latestCompetencyMet -> {
                                                    final Float currentValue = latestCompetencyMet.getValue();
                                                    if (actionContext.getOperator() == MutationOperator.ADD) {
                                                        // add the current value to the previous value and return it
                                                        return currentValue + contextValue;
                                                    } else {
                                                        // subtract the previous value from the current value?
                                                        // TODO this feels like it should be the other way around
                                                        // TODO IMPORTANT! check this with product before merging, I think this might be a current bug
                                                        return currentValue - contextValue;
                                                    }
                                                })
                                                // Make sure awarded value is between 0...1
                                                .map(awardedValue -> Math.min(Math.max(0, awardedValue), 1));
                                }
                                return Mono.just(contextValue);
                            })
                            // alright now we have our awarded value, let's save it
                            .flatMap(awardedValue -> competencyMetService.create(studentId,
                                    deployment.getId(),
                                    deployment.getChangeId(),
                                    learnerWalkable.getId(),
                                    learnerWalkable.getElementType(),
                                    response.getWalkableEvaluationResult().getId(),
                                    documentId,
                                    document.getDocumentVersionId(),
                                    documentItemId,
                                    request.getAttempt().getId(),
                                    awardedValue,
                                    1F // the competency met is awarded from an action therefore we are 100% confident
                            ))
                            // alright, from here onwards things are pretty heavy. competency met are update recursively
                            // for all the parents, this is definitely a performance hit that needs to be reviewed and addressed
                            .flatMap(awarded -> computeCompetencyMetValueForParentsOf(documentItemId, awarded)
                                    .collectList())
                            .map(competenciesMet -> new EmptyActionResult(changeCompetencyMetAction));
                })
                // this is a complex operation that should be simplified, log any error that happens
                .doOnEach(log.reactiveErrorThrowable("error awarding competency met", throwable -> new HashMap<String, Object>() {
                    {put("studentId", studentId);}
                    {put("deploymentId", deployment.getId());}
                    {put("elementId", learnerWalkable.getId());}
                    {put("elementType", learnerWalkable.getElementType());}
                    {put("evaluationId", response.getWalkableEvaluationResult().getId());}
                }));
    }

    @Override
    public Mono<ActionConsumerOptions> getActionConsumerOptions() {
        return Mono.just(options);
    }

    /**
     * Compute the competency met for each parent of the given document item
     *
     * @param documentItemId the document item id to find the parents and compute the awareded competency for
     * @param awarded the initial awarded competency
     */
    private Flux<CompetencyMet> computeCompetencyMetValueForParentsOf(final UUID documentItemId, final CompetencyMet awarded) {
        return learnerCompetencyDocumentService.findAssociationsFrom(documentItemId, AssociationType.IS_CHILD_OF)
                .flatMap(itemAssociation -> computeCompetencyMetValue(itemAssociation, awarded));
    }

    /**
     * Compute the competency met for a parent document item. The idea is to get all the competency met entries for this item
     * children sum all values and divide by the total number of children. This will give a value over the progress on
     * a parent competency before it is awared.
     *
     * @param element the item association that represent the parent relationship with the awarded document item
     * @param awarded the competency met that has been awared in the first place, carries over necessary information to build
     *                parent competencyMet
     */
    @SuppressWarnings("Duplicates")
    private Flux<CompetencyMet> computeCompetencyMetValue(final ItemAssociation element, final CompetencyMet awarded) {
        final UUID documentItemId = element.getDestinationItemId();
        final UUID documentId = element.getDocumentId();
        final UUID studentId = awarded.getStudentId();

        // find all the children
        return learnerCompetencyDocumentService.findAssociationsTo(documentItemId, AssociationType.IS_CHILD_OF)
                .map(ItemAssociation::getOriginItemId)
                // find all the competency met for each children
                .flatMap(itemId -> competencyMetService.findLatest(studentId, documentId, itemId)
                        // create a default one, we will need it to calculate the completion
                        .defaultIfEmpty(new CompetencyMetByStudent()))
                .collectList()
                // compute the value
                .flatMap(competencies -> {
                    int total = competencies.size();

                    // compute the sum of all values
                    double valueSum = competencies.stream()
                            .filter(one -> one.getValue() != null)
                            // filter out empty defaulted competency met
                            .mapToDouble(CompetencyMetByStudent::getValue)
                            .sum();

                    // divide by the total number of children to get the competency met value
                    float value = (float) (valueSum / total);
                    float confidence = 1f; // since competency are awarded by actions the confidence is always 100%
                    // sanity check.
                    value = Math.min(value, 1.0f);
                    // persist the competency met for this document item
                    return competencyMetService.create(
                            studentId,
                            awarded.getDeploymentId(),
                            awarded.getChangeId(),
                            awarded.getCoursewareElementId(),
                            awarded.getCoursewareElementType(),
                            awarded.getEvaluationId(),
                            documentId,
                            awarded.getDocumentVersionId(),
                            documentItemId,
                            awarded.getAttemptId(),
                            value,
                            confidence
                    );
                })
                // compute competency met for each parent of this item
                .thenMany(computeCompetencyMetValueForParentsOf(documentItemId, awarded));
    }
}
