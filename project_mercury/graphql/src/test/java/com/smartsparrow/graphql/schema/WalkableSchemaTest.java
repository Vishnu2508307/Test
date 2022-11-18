package com.smartsparrow.graphql.schema;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;
import static com.smartsparrow.iam.IamDataStub.STUDENT_A_ID;
import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.ConfigurationField;
import com.smartsparrow.courseware.data.Interactive;
import com.smartsparrow.courseware.data.Walkable;
import com.smartsparrow.courseware.data.WalkableChild;
import com.smartsparrow.courseware.lang.InteractiveNotFoundException;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.courseware.pathway.LearnerPathwayMock;
import com.smartsparrow.courseware.pathway.Pathway;
import com.smartsparrow.courseware.pathway.PathwayMock;
import com.smartsparrow.courseware.pathway.PathwayType;
import com.smartsparrow.courseware.pathway.PreloadPathway;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.InteractiveService;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.iam.IamDataStub;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.learner.data.CompletedWalkable;
import com.smartsparrow.learner.data.Evaluation;
import com.smartsparrow.learner.data.LearnerActivity;
import com.smartsparrow.learner.data.LearnerInteractive;
import com.smartsparrow.learner.data.LearnerWalkable;
import com.smartsparrow.learner.data.Score;
import com.smartsparrow.learner.data.ScoreReason;
import com.smartsparrow.learner.data.StudentManualGradeReport;
import com.smartsparrow.learner.service.CoursewareHistoryService;
import com.smartsparrow.learner.service.EvaluationResultService;
import com.smartsparrow.learner.service.LearnerActivityService;
import com.smartsparrow.learner.service.LearnerInteractiveService;
import com.smartsparrow.learner.service.LearnerService;
import com.smartsparrow.learner.service.StudentScoreService;

import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.execution.relay.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class WalkableSchemaTest {

    @InjectMocks
    private WalkableSchema walkableSchema;

    @Mock
    private LearnerActivityService learnerActivityService;
    @Mock
    private LearnerInteractiveService learnerInteractiveService;
    @Mock
    private AuthenticationContextProvider authenticationContextProvider;
    @Mock
    private CoursewareHistoryService coursewareHistoryService;
    @Mock
    private EvaluationResultService evaluationResultService;
    @Mock
    private LearnerService learnerService;
    @Mock
    private StudentScoreService studentScoreService;
    @Mock
    private PathwayService pathwayService;
    @Mock
    private ActivityService activityService;
    @Mock
    private InteractiveService interactiveService;
    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;
    private ResolutionEnvironment resolutionEnvironment;

    private static final UUID pathwayId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final String config = "{\"foo\":\"bar\"}";
    private static final LearnerPathway linearPathway = LearnerPathwayMock.mockLearnerPathway(pathwayId,
                                                                                              PathwayType.LINEAR,
                                                                                              deploymentId, changeId, config, PreloadPathway.NONE);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(IamDataStub.STUDENT_A);
        resolutionEnvironment = new ResolutionEnvironment(
                null,
                newDataFetchingEnvironment()
                        .context(new BronteGQLContext()
                                         .setMutableAuthenticationContext(mutableAuthenticationContext)
                                         .setAuthenticationContext(authenticationContext)).build(),
                null,
                null,
                null,
                null);
    }

    @Test
    @DisplayName("should return an empty list when no children walkables exist")
    void getLearnerWalkables_noneFound() {
        when(linearPathway.supplyRelevantWalkables(eq(STUDENT_A_ID))).thenReturn(Flux.empty());

        Page<LearnerWalkable> result = walkableSchema
                .getLearnerWalkables(resolutionEnvironment, linearPathway, null, null)
                .join();

        assertNotNull(result);
        assertNotNull(result.getEdges());
        assertTrue(result.getEdges().isEmpty());
    }

    @Test
    @DisplayName("should return an activity as the walkable")
    void getLearnerWalkables_activity() {
        UUID activityId = UUID.randomUUID();
        WalkableChild c1 = new WalkableChild().setElementId(activityId).setElementType(ACTIVITY);
        when(linearPathway.supplyRelevantWalkables(eq(STUDENT_A_ID))).thenReturn(Flux.just(c1));
        when(learnerActivityService.findActivity(activityId, deploymentId)).thenReturn(Mono.just(new LearnerActivity()));

        Page<LearnerWalkable> result = walkableSchema
                .getLearnerWalkables(resolutionEnvironment, linearPathway, null, null)
                .join();

        assertNotNull(result);
        assertNotNull(result.getEdges());
        assertEquals(1, result.getEdges().size());
        assertTrue(result.getEdges().get(0).getNode() instanceof LearnerActivity);
    }

    @Test
    @DisplayName("should return an interactive as the walkable")
    void getLearnerWalkables_interactive() {
        UUID interactiveId = UUID.randomUUID();
        WalkableChild c1 = new WalkableChild().setElementId(interactiveId).setElementType(INTERACTIVE);
        when(linearPathway.supplyRelevantWalkables(eq(STUDENT_A_ID))).thenReturn(Flux.just(c1));
        when(learnerInteractiveService.findInteractive(interactiveId, deploymentId)).thenReturn(Mono.just(new LearnerInteractive()));

        Page<LearnerWalkable> result = walkableSchema
                .getLearnerWalkables(resolutionEnvironment, linearPathway, null, null)
                .join();

        assertNotNull(result);
        assertNotNull(result.getEdges());
        assertEquals(1, result.getEdges().size());
        assertTrue(result.getEdges().get(0).getNode() instanceof LearnerInteractive);
    }

    @Test
    void getHistory_pathwayAttemptIdNull() {
        when(coursewareHistoryService.fetchHistory(eq(linearPathway), eq(STUDENT_A_ID)))
                .thenReturn(Flux.just(new CompletedWalkable()));

        Page<CompletedWalkable> history = walkableSchema
                .getHistory(resolutionEnvironment, linearPathway, null, null, null)
                .join();

        assertNotNull(history);
        assertEquals(1, history.getEdges().size());

        verify(coursewareHistoryService).fetchHistory(linearPathway, STUDENT_A_ID);
    }

    @Test
    void getHistory_pathwayAttemptIdDefined() {
        UUID parentPathwayAttempt = UUID.randomUUID();
        when(coursewareHistoryService.fetchHistory(linearPathway, STUDENT_A_ID, parentPathwayAttempt))
                .thenReturn(Flux.just(new CompletedWalkable()));

        Page<CompletedWalkable> history = walkableSchema
                .getHistory(resolutionEnvironment, linearPathway, parentPathwayAttempt, null, null)
                .join();

        assertNotNull(history);
        assertEquals(1, history.getEdges().size());

        verify(coursewareHistoryService).fetchHistory(linearPathway, STUDENT_A_ID, parentPathwayAttempt);
    }

    @Test
    void getHistory_noHistoryFound() {
        when(coursewareHistoryService.fetchHistory(eq(linearPathway), eq(STUDENT_A_ID)))
                .thenReturn(Flux.empty());

        Page<CompletedWalkable> history = walkableSchema
                .getHistory(resolutionEnvironment, linearPathway, null, null, null)
                .join();

        assertNotNull(history);
        assertEquals(0, history.getEdges().size());
    }

    @Test
    void getEvaluationData() {
        UUID evaluationId = UUID.randomUUID();
        when(evaluationResultService.fetch(evaluationId)).thenReturn(Mono.just(new Evaluation()));

        Evaluation evaluationData = walkableSchema
                .getEvaluationData(new CompletedWalkable().setEvaluationId(evaluationId))
                .join();

        assertNotNull(evaluationData);

        verify(evaluationResultService).fetch(evaluationId);
    }

    @SuppressWarnings("unchecked")
    @Test
    void getFields_emptyList() {
        CompletedWalkable completedWalkable = new CompletedWalkable()
                .setElementId(pathwayId)
                .setChangeId(changeId)
                .setDeploymentId(deploymentId);

        List<String> fields = new ArrayList<>();

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> walkableSchema
                .getCompletedWalkableFields(completedWalkable, fields)
                .join());
        assertNotNull(e);
        assertEquals("at least 1 field name must be supplied", e.getMessage());
        verify(learnerService, never()).fetchFields(eq(deploymentId), eq(changeId), eq(pathwayId), any(List.class));
    }

    @Test
    void getCompletedWalkableFields() {
        CompletedWalkable completedWalkable = new CompletedWalkable()
                .setElementId(pathwayId)
                .setChangeId(changeId)
                .setDeploymentId(deploymentId);

        List<String> fields = Lists.newArrayList("foo");

        when(learnerService.fetchFields(deploymentId, changeId, pathwayId, fields))
                .thenReturn(Flux.just(
                   new ConfigurationField()
                           .setFieldName("foo")
                           .setFieldValue("aya")
                ));

        List<ConfigurationField> all = walkableSchema.getCompletedWalkableFields(completedWalkable, fields).join();

        assertNotNull(all);
        assertEquals(1, all.size());

        verify(learnerService).fetchFields(deploymentId, changeId, pathwayId, fields);
    }

    @Test
    void getScore() {
        final UUID activityId = UUID.randomUUID();
        LearnerWalkable learnerWalkable = mock(LearnerWalkable.class);
        when(learnerWalkable.getDeploymentId()).thenReturn(deploymentId);
        when(learnerWalkable.getChangeId()).thenReturn(changeId);
        when(learnerWalkable.getId()).thenReturn(activityId);

        when(studentScoreService.computeScore(deploymentId, IamDataStub.STUDENT_A.getId(), activityId, null))
                .thenReturn(Mono.just(new Score()
                        .setReason(ScoreReason.SCORED)
                        .setValue(75d)));

        Score score = walkableSchema.getScore(resolutionEnvironment, learnerWalkable).join();

        assertNotNull(score);
        assertEquals(Double.valueOf(75d), score.getValue());
        assertEquals(ScoreReason.SCORED, score.getReason());
    }

    @Test
    void getWalkableFields() {
        UUID activityId = UUID.randomUUID();
        LearnerWalkable learnerWalkable = mock(LearnerWalkable.class);
        when(learnerWalkable.getId()).thenReturn(activityId);
        when(learnerWalkable.getChangeId()).thenReturn(changeId);
        when(learnerWalkable.getDeploymentId()).thenReturn(deploymentId);

        List<String> fields = Lists.newArrayList("foo");

        when(learnerService.fetchFields(deploymentId, changeId, activityId, fields))
                .thenReturn(Flux.just(
                        new ConfigurationField()
                                .setFieldName("foo")
                                .setFieldValue("aya")
                ));

        List<ConfigurationField> all = walkableSchema.getLearnerWalkableFields(learnerWalkable, fields).join();

        assertNotNull(all);
        assertEquals(1, all.size());

        verify(learnerService).fetchFields(deploymentId, changeId, activityId, fields);
    }

    @Test
    void getCompletedWalkable_nullAttemptId() {
        StudentManualGradeReport manualGradeReport = new StudentManualGradeReport();

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> walkableSchema
                .getCompletedWalkable(manualGradeReport)
                .join());

        assertNotNull(f);
        assertEquals("attemptId is required", f.getMessage());
    }

    @Test
    void getCompletedWalkable() {
        UUID studentId = UUID.randomUUID();
        UUID elementId = UUID.randomUUID();
        UUID attemptId = UUID.randomUUID();

        StudentManualGradeReport manualGradeReport = new StudentManualGradeReport()
                .setDeploymentId(deploymentId)
                .setStudentId(studentId)
                .setParentId(elementId)
                .setAttemptId(attemptId);

        when(coursewareHistoryService.findCompletedWalkable(deploymentId, studentId, elementId, attemptId))
                .thenReturn(Mono.just(new CompletedWalkable()));

        CompletedWalkable found = walkableSchema.getCompletedWalkable(manualGradeReport).join();

        assertNotNull(found);

        verify(coursewareHistoryService).findCompletedWalkable(deploymentId, studentId, elementId, attemptId);
    }

    @Test
    void getWalkables_all() {
        Pathway pathway = PathwayMock.mockPathway(pathwayId);

        WalkableChild one = new WalkableChild()
                .setElementId(UUID.randomUUID())
                .setElementType(INTERACTIVE);
        WalkableChild two = new WalkableChild()
                .setElementId(UUID.randomUUID())
                .setElementType(ACTIVITY);

        when(pathwayService.getOrderedWalkableChildren(pathwayId))
                .thenReturn(Mono.just(Lists.newArrayList(one, two)));

        when(activityService.findById(any(UUID.class))).thenReturn(Mono.just(new Activity()));
        when(interactiveService.findById(any(UUID.class))).thenReturn(Mono.just(new Interactive()));

        Page<Walkable> page = walkableSchema.getWalkables(pathway, null, null).join();

        assertNotNull(page);

        assertEquals(2, page.getEdges().size());
    }

    @Test
    void getWalkables_partial() {
        Pathway pathway = PathwayMock.mockPathway(pathwayId);

        WalkableChild one = new WalkableChild()
                .setElementId(UUID.randomUUID())
                .setElementType(INTERACTIVE);
        WalkableChild two = new WalkableChild()
                .setElementId(UUID.randomUUID())
                .setElementType(ACTIVITY);

        when(pathwayService.getOrderedWalkableChildren(pathwayId))
                .thenReturn(Mono.just(Lists.newArrayList(one, two)));

        when(activityService.findById(any(UUID.class))).thenReturn(Mono.just(new Activity()));
        TestPublisher<Interactive> publisher = TestPublisher.create();
        publisher.error(new InteractiveNotFoundException(one.getElementId()));

        when(interactiveService.findById(any(UUID.class))).thenReturn(publisher.mono());

        Page<Walkable> page = walkableSchema.getWalkables(pathway, null, null).join();

        assertNotNull(page);

        assertEquals(1, page.getEdges().size());
    }
}
