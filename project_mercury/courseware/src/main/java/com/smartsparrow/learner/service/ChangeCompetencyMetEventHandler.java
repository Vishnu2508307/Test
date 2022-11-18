package com.smartsparrow.learner.service;

import static com.smartsparrow.courseware.route.CoursewareRoutes.EVALUATION_EVENT_MESSAGE;
import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.competency.data.AssociationType;
import com.smartsparrow.competency.data.ItemAssociation;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.action.competency.ChangeCompetencyMetAction;
import com.smartsparrow.eval.action.competency.ChangeCompetencyMetActionContext;
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.learner.data.CompetencyMet;
import com.smartsparrow.learner.data.CompetencyMetByStudent;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.data.LearnerDocument;
import com.smartsparrow.learner.event.EvaluationEventMessage;

public class ChangeCompetencyMetEventHandler {

    private static final Logger log = LoggerFactory.getLogger(ChangeCompetencyMetEventHandler.class);

    private final CompetencyMetService competencyMetService;
    private final LearnerCompetencyDocumentService learnerCompetencyDocumentService;

    @Inject
    public ChangeCompetencyMetEventHandler(CompetencyMetService competencyMetService,
                                        LearnerCompetencyDocumentService learnerCompetencyDocumentService) {
        this.competencyMetService = competencyMetService;
        this.learnerCompetencyDocumentService = learnerCompetencyDocumentService;
    }

    /**
     * Handle the assignment of an awarded competency document item via an action
     *
     * @param exchange the exchange to extract the event and action from (a circular reference between document item
     *                 association of type {@link AssociationType#IS_CHILD_OF})
     * @throws StackOverflowError when the recursion is infinite in the document map
     * @throws IllegalArgumentFault when the document is not found
     */
    @Handler
    public void handle(Exchange exchange) {
        // extract needed objects from exchange
        final EvaluationEventMessage eventMessage = exchange.getProperty(EVALUATION_EVENT_MESSAGE, EvaluationEventMessage.class);
        final ChangeCompetencyMetAction action = exchange.getIn().getBody(ChangeCompetencyMetAction.class);
        final ChangeCompetencyMetActionContext context = action.getContext();

        final EvaluationResult evaluationResult = eventMessage.getEvaluationResult();

        final LearnerDocument document = learnerCompetencyDocumentService.findDocument(context.getDocumentId())
                .block();

        final UUID documentId = context.getDocumentId();
        final UUID documentItemId = context.getDocumentItemId();

        UUID studentId = eventMessage.getStudentId();

        log.info("About to award competency met for document {}, documentItem {} to student {}", documentId, documentItemId,
                studentId);

        affirmArgument(document != null, "document not found");

        // Calculate new competency met awarded value
        Float awardedValue = 0f;
        Float contextValue = context.getValue();
        switch (context.getOperator()){
        case SET:
            awardedValue = contextValue;
            break;
        case ADD:
        case REMOVE:
            // need tp find latest competency met to check for already existing value, or default to 0F if none found
            CompetencyMetByStudent latestStoredCompetencyMet = competencyMetService.findLatest(studentId, documentId, documentItemId)
                    .defaultIfEmpty(new CompetencyMetByStudent().setValue(0f))
                    .block();
            Float currentValue = latestStoredCompetencyMet != null ? latestStoredCompetencyMet.getValue() : 0;
            if (context.getOperator() == MutationOperator.ADD) {
                awardedValue = currentValue + contextValue;
            } else {
                awardedValue = currentValue - contextValue;
            }
            break;
        // no 'default' needed, the ActionSerializer guarantees these 3 options are always met
        }

        // Make sure awarded value is between 0...1
        awardedValue = Math.min(Math.max(0, awardedValue), 1);

        // create a new competency met entry for this student - well done mate!
        CompetencyMet awarded = competencyMetService.create(studentId,
                eventMessage.getDeploymentId(),
                eventMessage.getChangeId(),
                evaluationResult.getCoursewareElementId(),
                CoursewareElementType.INTERACTIVE, // FIXME attention this is assumed to always be interactive change later if other types are required
                evaluationResult.getId(),
                documentId,
                document.getDocumentVersionId(),
                documentItemId,
                eventMessage.getAttemptId(),
                awardedValue,
                1F // the competency met is awarded from an action therefore we are 100% confident
        ).block();

        // compute the competency met for each parent
        computeCompetencyMetValueForParentsOf(documentItemId, awarded);
    }

    /**
     * Compute the competency met for each parent of the given document item
     *
     * @param documentItemId the document item id to find the parents and compute the awareded competency for
     * @param awarded the initial awarded competency
     */
    private void computeCompetencyMetValueForParentsOf(final UUID documentItemId, final CompetencyMet awarded) {
        List<ItemAssociation> parents = learnerCompetencyDocumentService.findAssociationsFrom(documentItemId, AssociationType.IS_CHILD_OF)
                .collectList()
                .block();

        affirmArgument(parents != null, "parents not found");

        parents.forEach(itemAssociation -> computeCompetencyMetValue(itemAssociation, awarded));
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
    private void computeCompetencyMetValue(final ItemAssociation element, final CompetencyMet awarded) {
        final UUID documentItemId = element.getDestinationItemId();
        final UUID documentId = element.getDocumentId();
        final UUID studentId = awarded.getStudentId();

        // find all the children
        List<UUID> children = learnerCompetencyDocumentService.findAssociationsTo(documentItemId, AssociationType.IS_CHILD_OF)
                .map(ItemAssociation::getOriginItemId)
                .collectList()
                .block();

        affirmArgument(children != null, "children not found");

        // find all the competency met for each children
        List<CompetencyMetByStudent> competencies = children.stream()
                .map(itemId -> competencyMetService.findLatest(studentId, documentId, itemId)
                        // if a child has not competency met entry create an empty one to keep the count
                        .defaultIfEmpty(new CompetencyMetByStudent())
                        .block())
                .collect(Collectors.toList());

        int total = competencies.size();

        // compute the sum of all values
        double valueSum = competencies.stream()
                .filter(one -> one.getValue() != null)
                .mapToDouble(CompetencyMetByStudent::getValue)
                .sum();

        // divide by the total number of children to get the competency met value
        float value = (float) (valueSum / total);
        float confidence = 1f; // since competency are awarded by actions the confidence is always 100%
        // sanity check.
        value = Math.min(value, 1.0f);

        // persist the competency met for this document item
        competencyMetService.create(
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
        ).block();

        // compute competency met for each parent of this item
        computeCompetencyMetValueForParentsOf(documentItemId, awarded);
    }
}
