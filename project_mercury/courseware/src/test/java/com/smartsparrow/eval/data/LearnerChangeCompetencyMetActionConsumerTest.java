package com.smartsparrow.eval.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.competency.data.AssociationType;
import com.smartsparrow.competency.data.ItemAssociation;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.competency.ChangeCompetencyMetAction;
import com.smartsparrow.eval.action.competency.ChangeCompetencyMetActionContext;
import com.smartsparrow.eval.action.progress.EmptyActionResult;
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.learner.attempt.Attempt;
import com.smartsparrow.learner.data.CompetencyMet;
import com.smartsparrow.learner.data.CompetencyMetByStudent;
import com.smartsparrow.learner.data.Deployment;
import com.smartsparrow.learner.data.LearnerDocument;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.service.CompetencyMetService;
import com.smartsparrow.learner.service.LearnerCompetencyDocumentService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class LearnerChangeCompetencyMetActionConsumerTest {

    @InjectMocks
    private LearnerChangeCompetencyMetActionConsumer consumer;

    @Mock
    private CompetencyMetService competencyMetService;

    @Mock
    private LearnerCompetencyDocumentService learnerCompetencyDocumentService;

    @Mock
    private LearnerWalkable learnerWalkable;
    @Mock
    private ChangeCompetencyMetAction changeCompetencyMetAction;
    @Mock
    private ChangeCompetencyMetActionContext actionContext;
    private LearnerEvaluationRequest request;
    private LearnerEvaluationResponse response;
    private LearnerEvaluationResponseContext responseContext;
    private static final UUID walkableId = UUIDs.timeBased();
    private static final Deployment deployment = new Deployment()
            .setId(UUIDs.timeBased())
            .setChangeId(UUIDs.timeBased());
    private static final UUID studentId = UUIDs.timeBased();
    private static final UUID documentId = UUIDs.timeBased();
    private static final UUID documentItemId = UUIDs.timeBased();
    private static final UUID evaluationId = UUIDs.timeBased();
    private static final UUID documentVersionId = UUIDs.timeBased();
    private static final UUID attemptId = UUIDs.timeBased();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(learnerWalkable.getId()).thenReturn(walkableId);
        when(learnerWalkable.getDeploymentId()).thenReturn(deployment.getId());
        when(learnerWalkable.getChangeId()).thenReturn(deployment.getChangeId());
        when(learnerWalkable.getElementType()).thenReturn(CoursewareElementType.INTERACTIVE);

        when(actionContext.getDocumentId()).thenReturn(documentId);
        when(actionContext.getDocumentItemId()).thenReturn(documentItemId);
        when(actionContext.getValue()).thenReturn(0.2F);
        when(actionContext.getOperator()).thenReturn(MutationOperator.SET);
        when(changeCompetencyMetAction.getContext()).thenReturn(actionContext);
        when(changeCompetencyMetAction.getType()).thenReturn(Action.Type.CHANGE_COMPETENCY);

        request = new LearnerEvaluationRequest()
                .setAttempt(new Attempt()
                        .setId(attemptId))
                .setLearnerWalkable(learnerWalkable)
                .setDeployment(deployment)
                .setStudentId(studentId);

        response = new LearnerEvaluationResponse()
                .setWalkableEvaluationResult(new WalkableEvaluationResult()
                        .setId(evaluationId))
                .setEvaluationRequest(request);

        responseContext = new LearnerEvaluationResponseContext()
                .setResponse(response);

        when(learnerCompetencyDocumentService.findDocument(documentId))
                .thenReturn(Mono.just(new LearnerDocument()
                        .setDocumentVersionId(documentVersionId)));

        when(competencyMetService.findLatest(studentId, documentId, documentItemId))
                .thenReturn(Mono.empty());
    }

    @Test
    void getActionConsumerOptions_notAsync() {
        ActionConsumerOptions options = consumer.getActionConsumerOptions()
                .block();
        assertNotNull(options);
        assertFalse(options.isAsync());
    }

    @Test
    void consume_operator_SET() {
        final UUID destinationId = UUIDs.timeBased();
        final UUID itemId = UUIDs.timeBased();
        when(competencyMetService.create(
                studentId,
                deployment.getId(),
                deployment.getChangeId(),
                walkableId,
                CoursewareElementType.INTERACTIVE,
                evaluationId,
                documentId,
                documentVersionId,
                documentItemId,
                attemptId,
                0.2F, 1F
        )).thenReturn(Mono.just(new CompetencyMet()
                .setDeploymentId(deployment.getId())
                .setChangeId(deployment.getChangeId())
                .setCoursewareElementId(walkableId)
                .setCoursewareElementType(CoursewareElementType.INTERACTIVE)
                .setEvaluationId(evaluationId)
                .setDocumentVersionId(documentVersionId)
                .setAttemptId(attemptId)
                .setStudentId(studentId)));

        when(learnerCompetencyDocumentService.findAssociationsFrom(documentItemId, AssociationType.IS_CHILD_OF))
                .thenReturn(Flux.just(new ItemAssociation()
                        .setDestinationItemId(destinationId)
                        .setDocumentId(documentId)));
        when(learnerCompetencyDocumentService.findAssociationsTo(destinationId, AssociationType.IS_CHILD_OF))
                .thenReturn(Flux.just(new ItemAssociation()
                        .setOriginItemId(itemId)));
        when(competencyMetService.findLatest(studentId, documentId, itemId)).thenReturn(Mono.empty());

        when(competencyMetService.create(
                studentId,
                deployment.getId(),
                deployment.getChangeId(),
                walkableId,
                CoursewareElementType.INTERACTIVE,
                evaluationId,
                documentId,
                documentVersionId,
                destinationId,
                attemptId,
                0F, 1F
        )).thenReturn(Mono.just(new CompetencyMet()
                .setStudentId(studentId)));

        when(learnerCompetencyDocumentService.findAssociationsFrom(destinationId, AssociationType.IS_CHILD_OF))
                .thenReturn(Flux.empty());

        final EmptyActionResult result = consumer.consume(changeCompetencyMetAction, responseContext)
                .block();

        assertNotNull(result);
        assertEquals(changeCompetencyMetAction, result.getValue());
        assertEquals(Action.Type.CHANGE_COMPETENCY, result.getType());

        verify(competencyMetService).create(studentId,
                deployment.getId(),
                deployment.getChangeId(),
                walkableId,
                CoursewareElementType.INTERACTIVE,
                evaluationId,
                documentId,
                documentVersionId,
                documentItemId,
                attemptId,
                0.2F, 1F);
        verify(competencyMetService).create(studentId,
                deployment.getId(),
                deployment.getChangeId(),
                walkableId,
                CoursewareElementType.INTERACTIVE,
                evaluationId,
                documentId,
                documentVersionId,
                destinationId,
                attemptId,
                0F, 1F);
    }

    @Test
    void consume_operator_ADD() {
        when(actionContext.getOperator()).thenReturn(MutationOperator.ADD);
        final UUID destinationId = UUIDs.timeBased();
        final UUID itemId = UUIDs.timeBased();
        when(competencyMetService.create(
                studentId,
                deployment.getId(),
                deployment.getChangeId(),
                walkableId,
                CoursewareElementType.INTERACTIVE,
                evaluationId,
                documentId,
                documentVersionId,
                documentItemId,
                attemptId,
                0.2F, 1F
        )).thenReturn(Mono.just(new CompetencyMet()
                                        .setDeploymentId(deployment.getId())
                                        .setChangeId(deployment.getChangeId())
                                        .setCoursewareElementId(walkableId)
                                        .setCoursewareElementType(CoursewareElementType.INTERACTIVE)
                                        .setEvaluationId(evaluationId)
                                        .setDocumentVersionId(documentVersionId)
                                        .setAttemptId(attemptId)
                                        .setStudentId(studentId)));

        when(learnerCompetencyDocumentService.findAssociationsFrom(documentItemId, AssociationType.IS_CHILD_OF))
                .thenReturn(Flux.just(new ItemAssociation()
                                              .setDestinationItemId(destinationId)
                                              .setDocumentId(documentId)));
        when(learnerCompetencyDocumentService.findAssociationsTo(destinationId, AssociationType.IS_CHILD_OF))
                .thenReturn(Flux.just(new ItemAssociation()
                                              .setOriginItemId(itemId)));
        when(competencyMetService.findLatest(studentId, documentId, itemId)).thenReturn(Mono.empty());

        when(competencyMetService.create(
                studentId,
                deployment.getId(),
                deployment.getChangeId(),
                walkableId,
                CoursewareElementType.INTERACTIVE,
                evaluationId,
                documentId,
                documentVersionId,
                destinationId,
                attemptId,
                0F, 1F
        )).thenReturn(Mono.just(new CompetencyMet()
                                        .setStudentId(studentId)));

        when(learnerCompetencyDocumentService.findAssociationsFrom(destinationId, AssociationType.IS_CHILD_OF))
                .thenReturn(Flux.empty());

        final EmptyActionResult result = consumer.consume(changeCompetencyMetAction, responseContext)
                .block();

        assertNotNull(result);
        assertEquals(changeCompetencyMetAction, result.getValue());
        assertEquals(Action.Type.CHANGE_COMPETENCY, result.getType());

        verify(competencyMetService).create(studentId,
                                            deployment.getId(),
                                            deployment.getChangeId(),
                                            walkableId,
                                            CoursewareElementType.INTERACTIVE,
                                            evaluationId,
                                            documentId,
                                            documentVersionId,
                                            documentItemId,
                                            attemptId,
                                            0.2F, 1F);
        verify(competencyMetService).create(studentId,
                                            deployment.getId(),
                                            deployment.getChangeId(),
                                            walkableId,
                                            CoursewareElementType.INTERACTIVE,
                                            evaluationId,
                                            documentId,
                                            documentVersionId,
                                            destinationId,
                                            attemptId,
                                            0F, 1F);
    }

    @Test
    void consume_operator_REMOVE() {
        when(actionContext.getOperator()).thenReturn(MutationOperator.REMOVE);
        final UUID destinationId = UUIDs.timeBased();
        final UUID itemId = UUIDs.timeBased();
        when(competencyMetService.create(
                studentId,
                deployment.getId(),
                deployment.getChangeId(),
                walkableId,
                CoursewareElementType.INTERACTIVE,
                evaluationId,
                documentId,
                documentVersionId,
                documentItemId,
                attemptId,
                0.10000001F, 1F
        )).thenReturn(Mono.just(new CompetencyMet()
                                        .setDeploymentId(deployment.getId())
                                        .setChangeId(deployment.getChangeId())
                                        .setCoursewareElementId(walkableId)
                                        .setCoursewareElementType(CoursewareElementType.INTERACTIVE)
                                        .setEvaluationId(evaluationId)
                                        .setDocumentVersionId(documentVersionId)
                                        .setAttemptId(attemptId)
                                        .setStudentId(studentId)));

        when(learnerCompetencyDocumentService.findAssociationsFrom(documentItemId, AssociationType.IS_CHILD_OF))
                .thenReturn(Flux.just(new ItemAssociation()
                                              .setDestinationItemId(destinationId)
                                              .setDocumentId(documentId)));
        when(learnerCompetencyDocumentService.findAssociationsTo(destinationId, AssociationType.IS_CHILD_OF))
                .thenReturn(Flux.just(new ItemAssociation()
                                              .setOriginItemId(itemId)));
        when(competencyMetService.findLatest(studentId, documentId, documentItemId)).thenReturn(Mono.just(new CompetencyMetByStudent()
                                                                                                                  .setDocumentItemId(documentItemId)
                                                                                                                  .setStudentId(studentId)
                                                                                                                  .setDocumentId(documentId)
                                                                                                                  .setValue(0.3F)));

        when(competencyMetService.findLatest(studentId, documentId, itemId)).thenReturn(Mono.just(new CompetencyMetByStudent()
                                                                                                          .setDocumentItemId(itemId)
                                                                                                          .setStudentId(studentId)
                                                                                                          .setDocumentId(documentId)
                                                                                                          .setValue(0.3F)));

        when(competencyMetService.create(
                studentId,
                deployment.getId(),
                deployment.getChangeId(),
                walkableId,
                CoursewareElementType.INTERACTIVE,
                evaluationId,
                documentId,
                documentVersionId,
                destinationId,
                attemptId,
                0.3F, 1F
        )).thenReturn(Mono.just(new CompetencyMet()
                                        .setStudentId(studentId)));

        when(learnerCompetencyDocumentService.findAssociationsFrom(destinationId, AssociationType.IS_CHILD_OF))
                .thenReturn(Flux.empty());

        final EmptyActionResult result = consumer.consume(changeCompetencyMetAction, responseContext)
                .block();

        assertNotNull(result);
        assertEquals(changeCompetencyMetAction, result.getValue());
        assertEquals(Action.Type.CHANGE_COMPETENCY, result.getType());

        verify(learnerCompetencyDocumentService).findDocument(documentId);
        verify(competencyMetService).findLatest(studentId, documentId, documentItemId);
        verify(competencyMetService, atLeastOnce()).findLatest(studentId, documentId, itemId);


        verify(competencyMetService).create(studentId,
                                            deployment.getId(),
                                            deployment.getChangeId(),
                                            walkableId,
                                            CoursewareElementType.INTERACTIVE,
                                            evaluationId,
                                            documentId,
                                            documentVersionId,
                                            documentItemId,
                                            attemptId, 0.10000001F, 1F);
        verify(competencyMetService).create(studentId,
                                            deployment.getId(),
                                            deployment.getChangeId(),
                                            walkableId,
                                            CoursewareElementType.INTERACTIVE,
                                            evaluationId,
                                            documentId,
                                            documentVersionId,
                                            destinationId,
                                            attemptId,
                                            0.3F, 1F);
    }

}