package com.smartsparrow.graphql.schema;

import static com.smartsparrow.courseware.pathway.LearnerPathwayMock.mockLearnerPathway;
import static com.smartsparrow.iam.IamTestUtils.mockAuthenticationContextProvider;
import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.lang.PathwayNotFoundException;
import com.smartsparrow.courseware.pathway.LearnerPathway;
import com.smartsparrow.courseware.pathway.Pathway;
import com.smartsparrow.courseware.pathway.PathwayType;
import com.smartsparrow.courseware.pathway.PreloadPathway;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.learner.data.LearnerActivity;
import com.smartsparrow.learner.data.Score;
import com.smartsparrow.learner.data.ScoreReason;
import com.smartsparrow.learner.service.LearnerActivityService;
import com.smartsparrow.learner.service.StudentScoreService;

import io.leangen.graphql.execution.ResolutionEnvironment;
import io.leangen.graphql.execution.relay.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class PathwaySchemaTest {

    @InjectMocks
    private PathwaySchema pathwaySchema;
    @Mock
    private LearnerActivityService learnerActivityService;
    @Mock
    private StudentScoreService studentScoreService;
    @Mock
    private AuthenticationContextProvider authenticationContextProvider;
    @Mock
    private ActivityService activityService;
    @Mock
    private PathwayService pathwayService;
    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;
    private ResolutionEnvironment resolutionEnvironment;

    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID activityId = UUID.randomUUID();
    private static final UUID pathwayId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final LearnerActivity activity = new LearnerActivity().setId(activityId).setDeploymentId(deploymentId);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockAuthenticationContextProvider(authenticationContextProvider, accountId);
        resolutionEnvironment = new ResolutionEnvironment(
                null,
                newDataFetchingEnvironment()
                        .context(new BronteGQLContext()
                                         .setMutableAuthenticationContext(mutableAuthenticationContext)
                                         .setAuthenticationContext(authenticationContextProvider.get())).build(),
                null,
                null,
                null,
                null);
    }

    @Test
    void getActivityLearnerPathways_all() {
        LearnerPathway one = mockLearnerPathway();
        LearnerPathway two = mockLearnerPathway();
        when(learnerActivityService.findChildPathways(activityId, deploymentId)).thenReturn(Flux.just(one, two));

        List<LearnerPathway> result = pathwaySchema.getWalkableLearnerPathways(activity, null).join();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getActivityLearnerPathways_noPathways() {
        when(learnerActivityService.findChildPathways(activityId, deploymentId)).thenReturn(Flux.empty());

        List<LearnerPathway> result = pathwaySchema.getWalkableLearnerPathways(activity, null).join();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getActivityLearnerPathways_pathwayId() {
        LearnerPathway learnerPathway = mockLearnerPathway();
        when(learnerActivityService.findChildPathway(activityId, deploymentId, pathwayId)) //
                .thenReturn(Mono.just(learnerPathway));

        List<LearnerPathway> result = pathwaySchema.getWalkableLearnerPathways(activity, pathwayId).join();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getActivityLearnerPathways_pathwayNotFound() {
        when(learnerActivityService.findChildPathway(activityId, deploymentId, pathwayId)).thenReturn(Mono.empty());

        List<LearnerPathway> result = pathwaySchema.getWalkableLearnerPathways(activity, pathwayId).join();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getStudentPathwayScore() {
        LearnerPathway learnerPathway = mockLearnerPathway(pathwayId, PathwayType.LINEAR, deploymentId, UUID.randomUUID(), null, PreloadPathway.NONE);

        when(studentScoreService.computeScore(deploymentId, accountId, pathwayId, null))
                .thenReturn(Mono.just(new Score()
                        .setValue(10d)
                        .setReason(ScoreReason.SCORED)));

        Score score = pathwaySchema.getStudentPathwayScore(resolutionEnvironment, learnerPathway).join();

        assertNotNull(score);
        assertEquals(Double.valueOf(10d), score.getValue());
        assertEquals(ScoreReason.SCORED, score.getReason());
    }

    @Test
    void getActivityPathways_all() {
        UUID pathwayOne = UUID.randomUUID();
        UUID pathwayTwo = UUID.randomUUID();
        UUID pathwayThree = UUID.randomUUID();

        List<UUID> childIds = Lists.newArrayList(pathwayOne, pathwayTwo, pathwayThree);

        when(activityService.findChildPathwayIds(activityId)).thenReturn(Mono.just(childIds));

        when(pathwayService.findById(any(UUID.class))).thenReturn(Mono.just(new Pathway() {
            @Override
            public UUID getId() {
                return null;
            }

            @Override
            public PathwayType getType() {
                return null;
            }
            @Override
            public PreloadPathway getPreloadPathway() {
                return null;
            }
        }));

        Page<Pathway> page = pathwaySchema.getActivityPathways(new Activity()
                .setId(activityId), null, null, null).join();

        assertNotNull(page);

        assertEquals(3, page.getEdges().size());
    }

    @Test
    void getActivityPathways_oneNotFound() {
        UUID pathwayOne = UUID.randomUUID();
        UUID pathwayTwo = UUID.randomUUID();
        UUID pathwayThree = UUID.randomUUID();

        List<UUID> childIds = Lists.newArrayList(pathwayOne, pathwayTwo, pathwayThree);

        when(activityService.findChildPathwayIds(activityId)).thenReturn(Mono.just(childIds));

        when(pathwayService.findById(any(UUID.class))).thenReturn(Mono.just(new Pathway() {
            @Override
            public UUID getId() {
                return null;
            }

            @Override
            public PathwayType getType() {
                return null;
            }

            @Override
            public PreloadPathway getPreloadPathway() {
                return null;
            }
        }));

        TestPublisher<Pathway> publisher = TestPublisher.create();
        publisher.error(new PathwayNotFoundException(pathwayThree));

        when(pathwayService.findById(pathwayThree)).thenReturn(publisher.mono());

        Page<Pathway> page = pathwaySchema.getActivityPathways(new Activity()
                .setId(activityId), null, null, null).join();

        assertNotNull(page);

        assertEquals(2, page.getEdges().size());
    }
}
