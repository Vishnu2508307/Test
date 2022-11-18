package com.smartsparrow.learner.service;

import static com.smartsparrow.courseware.route.CoursewareRoutes.EVALUATION_EVENT_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.competency.data.AssociationType;
import com.smartsparrow.competency.data.ItemAssociation;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.eval.action.Action;
import com.smartsparrow.eval.action.competency.ChangeCompetencyMetAction;
import com.smartsparrow.eval.action.competency.ChangeCompetencyMetActionContext;
import com.smartsparrow.eval.mutation.MutationOperator;
import com.smartsparrow.eval.parser.LiteralContext;
import com.smartsparrow.eval.resolver.Resolver;
import com.smartsparrow.learner.data.CompetencyMet;
import com.smartsparrow.learner.data.CompetencyMetByStudent;
import com.smartsparrow.learner.data.EvaluationResult;
import com.smartsparrow.learner.data.LearnerDocument;
import com.smartsparrow.learner.event.EvaluationEventMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class ChangeCompetencyMetEventHandlerTest {

    @InjectMocks
    private ChangeCompetencyMetEventHandler handler;

    @Mock
    private CompetencyMetService competencyMetService;

    @Mock
    private LearnerCompetencyDocumentService learnerCompetencyDocumentService;

    @Mock Message message;

    private Exchange exchange;
    private ItemAssociation parentAssociationOne;
    private CompetencyMet awarded;

    private static final UUID studentId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final UUID documentId = UUID.randomUUID();
    private static final UUID documentItemId = UUID.randomUUID();
    private static final UUID documentVersionId = UUID.randomUUID();
    private static final UUID evaluationId = UUID.randomUUID();
    private static final UUID attemptId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID childOneId = UUID.randomUUID();
    private static final UUID childTwoId = UUID.randomUUID();
    private static final UUID parentId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        parentAssociationOne = new ItemAssociation()
                .setDocumentId(documentId)
                .setDestinationItemId(parentId);

        exchange = mock(Exchange.class);
        EvaluationEventMessage eventMessage = mock(EvaluationEventMessage.class);

        EvaluationResult evaluationResult = new EvaluationResult()
                .setId(evaluationId)
                .setCoursewareElementId(elementId);

        awarded = new CompetencyMet()
                .setStudentId(studentId)
                .setDeploymentId(deploymentId)
                .setChangeId(changeId)
                .setCoursewareElementId(elementId)
                .setCoursewareElementType(CoursewareElementType.INTERACTIVE)
                .setEvaluationId(evaluationId)
                .setDocumentVersionId(documentVersionId)
                .setAttemptId(attemptId);

        when(eventMessage.getStudentId()).thenReturn(studentId);
        when(eventMessage.getDeploymentId()).thenReturn(deploymentId);
        when(eventMessage.getChangeId()).thenReturn(changeId);
        when(eventMessage.getAttemptId()).thenReturn(attemptId);
        when(eventMessage.getEvaluationResult()).thenReturn(evaluationResult);
        when(exchange.getProperty(EVALUATION_EVENT_MESSAGE, EvaluationEventMessage.class)).thenReturn(eventMessage);
        when(exchange.getIn()).thenReturn(message);

        when(learnerCompetencyDocumentService.findDocument(documentId)).thenReturn(Mono.just(
                new LearnerDocument().setDocumentVersionId(documentVersionId)
        ));

        when(competencyMetService.create(
                eq(studentId),
                eq(deploymentId),
                eq(changeId),
                eq(elementId),
                eq(CoursewareElementType.INTERACTIVE),
                eq(evaluationId),
                eq(documentId),
                eq(documentVersionId),
                eq(documentItemId),
                eq(attemptId),
                anyFloat(),
                anyFloat()
        )).thenReturn(Mono.just(awarded));

        when(learnerCompetencyDocumentService.findAssociationsFrom(documentItemId, AssociationType.IS_CHILD_OF))
                .thenReturn(Flux.just(parentAssociationOne));

        when(learnerCompetencyDocumentService.findAssociationsFrom(parentId, AssociationType.IS_CHILD_OF))
                .thenReturn(Flux.empty());

        when(learnerCompetencyDocumentService.findAssociationsTo(parentId, AssociationType.IS_CHILD_OF))
                .thenReturn(Flux.just(
                        new ItemAssociation().setOriginItemId(documentItemId), new ItemAssociation().setOriginItemId(childOneId), new ItemAssociation().setOriginItemId(childTwoId)
                ));

        when(competencyMetService.findLatest(studentId, documentId, documentItemId)).thenReturn(Mono.just(
                new CompetencyMetByStudent()
                .setValue(1F)
        ));

        when(competencyMetService.findLatest(studentId, documentId, childOneId)).thenReturn(Mono.empty());
        when(competencyMetService.findLatest(studentId, documentId, childTwoId)).thenReturn(Mono.just(
                new CompetencyMetByStudent()
                        .setValue(0.6666667f)
        ));

        when(competencyMetService.create(
                eq(studentId),
                eq(deploymentId),
                eq(changeId),
                eq(elementId),
                eq(CoursewareElementType.INTERACTIVE),
                eq(evaluationId),
                eq(documentId),
                eq(documentVersionId),
                eq(parentAssociationOne.getDestinationItemId()),
                eq(attemptId),
                anyFloat(),
                anyFloat()
        )).thenReturn(Mono.just(new CompetencyMet()));
    }

    private void mockAction(MutationOperator set, float value) {
        ChangeCompetencyMetActionContext changeCompetencyMetActionContext = new ChangeCompetencyMetActionContext()
                .setDocumentId(documentId)
                .setDocumentItemId(documentItemId)
                .setOperator(set)
                .setValue(value);

        ChangeCompetencyMetAction changeCompetencyMetAction = new ChangeCompetencyMetAction()
                .setContext(changeCompetencyMetActionContext)
                .setResolver(new LiteralContext().setType(Resolver.Type.LITERAL))
                .setType(Action.Type.SET_COMPETENCY);

        when(message.getBody(ChangeCompetencyMetAction.class)).thenReturn(changeCompetencyMetAction);
    }

    @Test
    void handle_set() {

        mockAction(MutationOperator.SET, 1.0f);

        ArgumentCaptor<Float> valueCaptor = ArgumentCaptor.forClass(Float.class);
        ArgumentCaptor<Float> confidenceCaptor = ArgumentCaptor.forClass(Float.class);

        handler.handle(exchange);

        verify(competencyMetService, times(1)).create(
                studentId,
                deploymentId,
                changeId,
                elementId,
                CoursewareElementType.INTERACTIVE,
                evaluationId,
                documentId,
                documentVersionId,
                documentItemId,
                attemptId,
                1F,
                1F
        );

        verify(competencyMetService, times(1)).create(
                eq(studentId),
                eq(deploymentId),
                eq(changeId),
                eq(elementId),
                eq(CoursewareElementType.INTERACTIVE),
                eq(evaluationId),
                eq(documentId),
                eq(documentVersionId),
                eq(parentAssociationOne.getDestinationItemId()),
                eq(attemptId),
                valueCaptor.capture(),
                confidenceCaptor.capture()
        );

        Float parentValue = valueCaptor.getValue();
        Float parentConfidence = confidenceCaptor.getValue();

        assertEquals(Float.valueOf(0.5555556f), parentValue);
        assertEquals(Float.valueOf(1f), parentConfidence);
    }

    @Test
    void handle_add_no_previous() {

        mockAction(MutationOperator.ADD, 1.0f);
        when(competencyMetService.findLatest(eq(studentId), eq(documentId), eq(documentItemId)))
                .thenReturn(Mono.empty());

        handler.handle(exchange);

        verify(competencyMetService, times(1)).create(
                studentId,
                deploymentId,
                changeId,
                elementId,
                CoursewareElementType.INTERACTIVE,
                evaluationId,
                documentId,
                documentVersionId,
                documentItemId,
                attemptId,
                1F,
                1F
        );

        verify(competencyMetService, times(1)).create(
                eq(studentId),
                eq(deploymentId),
                eq(changeId),
                eq(elementId),
                eq(CoursewareElementType.INTERACTIVE),
                eq(evaluationId),
                eq(documentId),
                eq(documentVersionId),
                eq(parentAssociationOne.getDestinationItemId()),
                eq(attemptId),
                any(),
                any()
        );

    }

    @Test
    void handle_add_with_previous() {

        mockAction(MutationOperator.ADD, 0.25F);
        when(competencyMetService.findLatest(eq(studentId), eq(documentId), eq(documentItemId)))
                .thenReturn(Mono.just(new CompetencyMetByStudent().setValue(0.25F)));

        handler.handle(exchange);

        verify(competencyMetService, times(1)).create(
                studentId,
                deploymentId,
                changeId,
                elementId,
                CoursewareElementType.INTERACTIVE,
                evaluationId,
                documentId,
                documentVersionId,
                documentItemId,
                attemptId,
                0.5F,
                1F
        );

        verify(competencyMetService, times(1)).create(
                eq(studentId),
                eq(deploymentId),
                eq(changeId),
                eq(elementId),
                eq(CoursewareElementType.INTERACTIVE),
                eq(evaluationId),
                eq(documentId),
                eq(documentVersionId),
                eq(parentAssociationOne.getDestinationItemId()),
                eq(attemptId),
                any(),
                any()
        );
    }

    @Test
    void handle_remove_no_previous() {

        mockAction(MutationOperator.REMOVE, 1F);
        when(competencyMetService.findLatest(eq(studentId), eq(documentId), eq(documentItemId)))
                .thenReturn(Mono.empty());

        handler.handle(exchange);

        verify(competencyMetService, times(1)).create(
                studentId,
                deploymentId,
                changeId,
                elementId,
                CoursewareElementType.INTERACTIVE,
                evaluationId,
                documentId,
                documentVersionId,
                documentItemId,
                attemptId,
                0F,
                1F
        );

        verify(competencyMetService, times(1)).create(
                eq(studentId),
                eq(deploymentId),
                eq(changeId),
                eq(elementId),
                eq(CoursewareElementType.INTERACTIVE),
                eq(evaluationId),
                eq(documentId),
                eq(documentVersionId),
                eq(parentAssociationOne.getDestinationItemId()),
                eq(attemptId),
                any(),
                any()
        );
    }

    @Test
    void handle_remove_with_previous() {

        mockAction(MutationOperator.REMOVE, 0.25F);
        when(competencyMetService.findLatest(eq(studentId), eq(documentId), eq(documentItemId)))
                .thenReturn(Mono.just(new CompetencyMetByStudent().setValue(1F)));

        handler.handle(exchange);

        verify(competencyMetService, times(1))
                .create(studentId,
                        deploymentId,
                        changeId,
                        elementId,
                        CoursewareElementType.INTERACTIVE,
                        evaluationId,
                        documentId,
                        documentVersionId,
                        documentItemId,
                        attemptId,
                        0.75F,
                        1F);

    verify(competencyMetService, times(1))
            .create(eq(studentId),
                    eq(deploymentId),
                    eq(changeId),
                    eq(elementId),
                    eq(CoursewareElementType.INTERACTIVE),
                    eq(evaluationId),
                    eq(documentId),
                    eq(documentVersionId),
                    eq(parentAssociationOne.getDestinationItemId()),
                    eq(attemptId),
                    any(),
                    any());
    }


    @Test
    void handle_value_over_range() {

        mockAction(MutationOperator.SET, 2);
        when(competencyMetService.findLatest(eq(studentId), eq(documentId), eq(documentItemId)))
                .thenReturn(Mono.empty());

        handler.handle(exchange);

        verify(competencyMetService, times(1))
                .create(studentId,
                        deploymentId,
                        changeId,
                        elementId,
                        CoursewareElementType.INTERACTIVE,
                        evaluationId,
                        documentId,
                        documentVersionId,
                        documentItemId,
                        attemptId,
                        1F,
                        1F);
    }

    @Test
    void handle_value_under_range() {

        mockAction(MutationOperator.SET, -2);
        when(competencyMetService.findLatest(eq(studentId), eq(documentId), eq(documentItemId)))
                .thenReturn(Mono.empty());

        handler.handle(exchange);

        verify(competencyMetService, times(1))
                .create(studentId,
                        deploymentId,
                        changeId,
                        elementId,
                        CoursewareElementType.INTERACTIVE,
                        evaluationId,
                        documentId,
                        documentVersionId,
                        documentItemId,
                        attemptId,
                        0F,
                        1F);
    }

    @Test
    void handle_infiniteRecursion() {
        UUID childThreeId = UUID.randomUUID();
        mockAction(MutationOperator.SET, 1);
        when(competencyMetService.findLatest(eq(studentId), eq(documentId), eq(documentItemId)))
                .thenReturn(Mono.empty());

        when(learnerCompetencyDocumentService.findAssociationsFrom(parentId, AssociationType.IS_CHILD_OF))
                .thenReturn(Flux.just(new ItemAssociation().setDestinationItemId(documentItemId).setDocumentId(documentId)));
        when(learnerCompetencyDocumentService.findAssociationsTo(documentItemId, AssociationType.IS_CHILD_OF))
                .thenReturn(Flux.just(
                        new ItemAssociation().setOriginItemId(childThreeId)
                ));
        when(competencyMetService.findLatest(studentId, documentId, childThreeId)).thenReturn(Mono.just(
                new CompetencyMetByStudent()
                .setValue(awarded.getValue())
        ));

        assertThrows(StackOverflowError.class, () -> handler.handle(exchange));
    }

}
