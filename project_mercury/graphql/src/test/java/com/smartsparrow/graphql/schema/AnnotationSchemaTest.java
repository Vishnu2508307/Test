package com.smartsparrow.graphql.schema;

import static graphql.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Provider;
import com.smartsparrow.annotation.service.Annotation;
import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.annotation.service.CoursewareAnnotation;
import com.smartsparrow.annotation.service.CoursewareAnnotationPayload;
import com.smartsparrow.annotation.service.DeploymentAnnotation;
import com.smartsparrow.annotation.service.LearnerAnnotation;
import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.graphql.BronteGQLContext;
import com.smartsparrow.graphql.auth.AllowCohortInstructor;
import com.smartsparrow.graphql.auth.AllowCoursewareElementContributorOrHigher;
import com.smartsparrow.graphql.auth.AllowCoursewareElementReviewerOrHigher;
import com.smartsparrow.graphql.auth.AllowEnrolledStudent;
import com.smartsparrow.graphql.auth.AllowWorkspaceReviewerOrHigher;
import com.smartsparrow.graphql.type.mutation.AnnotationArg;
import com.smartsparrow.iam.exception.PermissionFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.MutableAuthenticationContext;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.lang.DeploymentNotFoundException;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.util.UUIDs;

import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class AnnotationSchemaTest {

    @InjectMocks
    private AnnotationSchema annotationSchema;

    @Mock
    private AllowWorkspaceReviewerOrHigher allowWorkspaceReviewerOrHigher;
    @Mock
    private AllowCohortInstructor allowCohortInstructor;
    @Mock
    private AllowEnrolledStudent allowEnrolledStudent;
    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;
    @Mock
    private AccountService accountService;
    @Mock
    private AnnotationService annotationService;
    @Mock
    private ActivityService activityService;
    @Mock
    private DeploymentService deploymentService;
    @Mock
    private AllowCoursewareElementReviewerOrHigher coursewareElementReviewerOrHigher;
    @Mock
    private AllowCoursewareElementContributorOrHigher coursewareElementContributorOrHigher;
    @Mock
    private MutableAuthenticationContext mutableAuthenticationContext;

    private ResolutionEnvironment resolutionEnvironment;

    private static final UUID rootElementId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID workspaceId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID cohortId = UUID.randomUUID();
    private static final UUID changeId = UUID.randomUUID();
    private static final Motivation motivation = Motivation.commenting;
    private static final Motivation identifyingMotivation = Motivation.identifying;
    private static final String validJsonString = "{}";
    private static final String invalidJsonString = "invalid";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account().setId(accountId));

        when(allowCohortInstructor.test(authenticationContext,cohortId)).thenReturn(true);
        when(allowEnrolledStudent.test(authenticationContext,cohortId)).thenReturn(true);

        when(activityService.findWorkspaceIdByActivity(rootElementId)).thenReturn(Mono.just(workspaceId));
        when(allowWorkspaceReviewerOrHigher.test(authenticationContext,workspaceId)).thenReturn(true);
        when(annotationService.findCoursewareAnnotation(rootElementId, elementId, motivation))
                .thenReturn(Flux.just(new CoursewareAnnotation()));
        when(annotationService.findCoursewareAnnotation(rootElementId, motivation, accountId))
                .thenReturn(Flux.just(new CoursewareAnnotationPayload(new CoursewareAnnotation())));
        when(annotationService.create(any(CoursewareAnnotation.class))).thenReturn(Flux.just(new Void[]{}));
        when(annotationService.create(any(LearnerAnnotation.class))).thenReturn(Flux.just(new Void[]{}));
        when(annotationService.fetchCoursewareAnnotation(any(UUID.class), any(UUID.class), any(Motivation.class), any(UUID.class)))
                .thenReturn(Flux.just(new CoursewareAnnotationPayload(new CoursewareAnnotation())));

        when(deploymentService.findDeployment(deploymentId)).thenReturn(Mono.just(new DeployedActivity()
                .setCohortId(cohortId)
                .setChangeId(changeId)));
        when(accountService.findById(any(UUID.class))).thenReturn(Flux.just(new Account()));

        when(annotationService.findLearnerAnnotation(any(UUID.class), any(UUID.class), any(Motivation.class), any(UUID.class)))
                .thenReturn(Flux.just(new LearnerAnnotation()));
        when(annotationService.findLearnerAnnotation(any(UUID.class), any(UUID.class), any(Motivation.class)))
                .thenReturn(Flux.just(new LearnerAnnotation()));
        when(annotationService.findDeploymentAnnotations(any(UUID.class), any(UUID.class), any(Motivation.class)))
                .thenReturn(Flux.just(new DeploymentAnnotation(new LearnerAnnotation(), changeId)));
        when(annotationService.findDeploymentAnnotations(any(UUID.class), any(UUID.class), any(Motivation.class), any(UUID.class)))
                .thenReturn(Flux.just(new DeploymentAnnotation(new LearnerAnnotation(), changeId)));

        resolutionEnvironment= new ResolutionEnvironment(null, newDataFetchingEnvironment()
                .context(new BronteGQLContext()
                                 .setMutableAuthenticationContext(mutableAuthenticationContext)
                                 .setAuthenticationContext(authenticationContextProvider.get())).build(),
                                                         null,
                                                         null,
                                                         null,
                                                         null);
    }

    @Test
    void getAnnotationsByCourseware_noPermission() {
        when(allowWorkspaceReviewerOrHigher.test(authenticationContextProvider.get(), workspaceId)).thenReturn(false);

        PermissionFault e = assertThrows(PermissionFault.class, () ->
                annotationSchema
                        .getAnnotationsByCourseware(resolutionEnvironment, rootElementId, elementId, motivation)
                        .join());

        assertNotNull(e);
        assertEquals("Unauthorized", e.getMessage());
    }

    @Test
    void getAnnotationsByCourseware_noElementId() {
        when(coursewareElementReviewerOrHigher.test(any(), any(), any())).thenReturn(true);
        List<? extends Annotation> found = annotationSchema
                .getAnnotationsByCourseware(resolutionEnvironment,rootElementId, null, motivation)
                .join();

        assertNotNull(found);
        assertEquals(1, found.size());
        verify(annotationService).findCoursewareAnnotation(rootElementId, motivation, accountId);
        verify(annotationService, never()).findCoursewareAnnotation(any(UUID.class), any(UUID.class), any(Motivation.class));
    }

    @Test
    void getAnnotationsByCourseware_withElementId() {
        when(coursewareElementReviewerOrHigher.test(any(), any(), any())).thenReturn(true);
        List<? extends Annotation> found = annotationSchema
                .getAnnotationsByCourseware(resolutionEnvironment,rootElementId, elementId, motivation)
                .join();

        assertNotNull(found);
        assertEquals(1, found.size());
        verify(annotationService, never()).findCoursewareAnnotation(any(UUID.class), any(Motivation.class), any(UUID.class));
        verify(annotationService).fetchCoursewareAnnotation(rootElementId, elementId, motivation, accountId);
    }

    @Test
    void createAnnotationForCourseware_noPermission() {
        AnnotationArg annotationArg = mock(AnnotationArg.class);
        when(annotationArg.getMotivation()).thenReturn(motivation);

        when(allowWorkspaceReviewerOrHigher.test(authenticationContextProvider.get(), workspaceId)).thenReturn(false);

        PermissionFault e = assertThrows(PermissionFault.class, () ->
                annotationSchema
                        .createAnnotationForCourseware(resolutionEnvironment,rootElementId, elementId, annotationArg)
                        .join());

        assertNotNull(e);
        assertEquals("Unauthorized", e.getMessage());
    }

    @Test
    void createAnnotationForCourseware_invalidBody() {
        AnnotationArg annotationArg = mock(AnnotationArg.class);

        when(annotationArg.getMotivation()).thenReturn(motivation);
        when(annotationArg.getBody()).thenReturn(invalidJsonString);
        when(coursewareElementReviewerOrHigher.test(any(),any(), any())).thenReturn(true);

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationSchema
                        .createAnnotationForCourseware(resolutionEnvironment,rootElementId, elementId, annotationArg)
                        .join());

        assertNotNull(e);
        assertEquals("invalid body json", e.getMessage());
    }

    @Test
    void createAnnotationForCourseware_invalidTarget() {
        AnnotationArg annotationArg = mock(AnnotationArg.class);

        when(annotationArg.getMotivation()).thenReturn(motivation);
        when(annotationArg.getBody()).thenReturn(validJsonString);
        when(annotationArg.getTarget()).thenReturn(invalidJsonString);
        when(coursewareElementReviewerOrHigher.test(any(),any(), any())).thenReturn(true);

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationSchema
                        .createAnnotationForCourseware(resolutionEnvironment,rootElementId, elementId, annotationArg)
                        .join());

        assertNotNull(e);
        assertEquals("invalid target json", e.getMessage());
    }

    @Test
    void createAnnotationForCourseware() {
        AnnotationArg annotationArg = mock(AnnotationArg.class);

        when(annotationArg.getBody()).thenReturn(validJsonString);
        when(annotationArg.getTarget()).thenReturn(validJsonString);
        when(annotationArg.getMotivation()).thenReturn(motivation);
        when(coursewareElementReviewerOrHigher.test(any(),any(), any())).thenReturn(true);

        CoursewareAnnotation created = (CoursewareAnnotation) annotationSchema
                .createAnnotationForCourseware(resolutionEnvironment,rootElementId, elementId, annotationArg)
                .join();

        assertNotNull(created);
        assertAll(() -> {
            assertNotNull(created.getId());
            assertNotNull(created.getVersion());
            assertEquals(motivation, created.getMotivation());
            assertEquals(rootElementId, created.getRootElementId());
            assertEquals(elementId, created.getElementId());
            assertNotNull(created.getBodyJson());
            assertEquals(validJsonString, created.getBody());
            assertNotNull(created.getTargetJson());
            assertEquals(validJsonString, created.getTarget());
            assertEquals(accountId, created.getCreatorAccountId());
        });

        verify(annotationService).create(any(CoursewareAnnotation.class));
    }

    @Test
    void updateAnnotationForCourseware() {
        UUID annotationId = UUIDs.timeBased();
        AnnotationArg arg = mock(AnnotationArg.class);
        when(arg.getId()).thenReturn(annotationId);
        when(arg.getBody()).thenReturn(validJsonString);
        when(arg.getTarget()).thenReturn(validJsonString);

        CoursewareAnnotation original = new CoursewareAnnotation() //
                .setId(annotationId)
                .setVersion(UUIDs.timeBased())
                .setMotivation(motivation)
                .setRootElementId(rootElementId)
                .setElementId(elementId)
                .setCreatorAccountId(UUIDs.timeBased());

        when(annotationService.findCoursewareAnnotation(annotationId)).thenReturn(Mono.just(original));
        when(allowWorkspaceReviewerOrHigher.test(any(),any(UUID.class))).thenReturn(true);
        when(coursewareElementReviewerOrHigher.test(any(),any(), any())).thenReturn(true);

        // run the method
        annotationSchema.updateAnnotationForCourseware(resolutionEnvironment,arg).join();

        ArgumentCaptor<CoursewareAnnotation> argument = ArgumentCaptor.forClass(CoursewareAnnotation.class);
        verify(annotationService).create(argument.capture());

        assertAll(() -> {
            CoursewareAnnotation onward = argument.getValue();
            // these become set
            assertNotNull(onward.getBodyJson());
            assertNotNull(onward.getTargetJson());

            assertNotEquals(original.getVersion(), onward.getVersion()); // new version should be installed

            // ensure these are copied over.
            assertEquals(original.getId(), onward.getId());
            assertEquals(original.getMotivation(), onward.getMotivation());
            assertEquals(original.getRootElementId(), onward.getRootElementId());
            assertEquals(original.getElementId(), onward.getElementId());
            assertEquals(original.getCreatorAccountId(), onward.getCreatorAccountId());

            assertNotEquals(original, onward);
        });
    }

    @Test
    void updateAnnotationForCourseware_missingAnnotationArgId() {
        AnnotationArg arg = mock(AnnotationArg.class);
        when(arg.getId()).thenReturn(null);

        assertThrows(IllegalArgumentFault.class, () -> annotationSchema
                .updateAnnotationForCourseware(resolutionEnvironment,arg)
                .join());
    }

    @Test
    void updateAnnotationForCourseware_annotationNotFound() {
        UUID annotationId = UUIDs.timeBased();
        AnnotationArg arg = mock(AnnotationArg.class);
        when(arg.getId()).thenReturn(annotationId);

        when(annotationService.findCoursewareAnnotation(annotationId)).thenReturn(Mono.empty());

        annotationSchema
                .updateAnnotationForCourseware(resolutionEnvironment,arg)
                .handle((coursewareAnnotation, throwable) -> {
                    assertEquals(PermissionFault.class, throwable.getClass());
                    return coursewareAnnotation;
                })
                .join();
    }

    @Test
    void deleteAnnotationForCourseware() {
        UUID annotationId = UUIDs.timeBased();
        AnnotationArg arg = mock(AnnotationArg.class);
        when(arg.getId()).thenReturn(annotationId);

        CoursewareAnnotation annotation = new CoursewareAnnotation() //
                .setId(annotationId) //
                .setCreatorAccountId(accountId);

        when(coursewareElementReviewerOrHigher.test(any(),any(), any())).thenReturn(true);
        when(annotationService.findCoursewareAnnotation(annotationId)).thenReturn(Mono.just(annotation));
        when(annotationService.deleteAnnotation(annotation)).thenReturn(Flux.empty());

        annotationSchema.deleteAnnotationForCourseware(resolutionEnvironment,arg).join();

        verify(annotationService).deleteAnnotation(eq(annotation));
    }

    @Test
    void deleteAnnotationForCourseware_noIdSpecified() {
        AnnotationArg arg = mock(AnnotationArg.class);
        when(arg.getId()).thenReturn(null);

        assertThrows(IllegalArgumentFault.class, () -> annotationSchema
                .deleteAnnotationForCourseware(resolutionEnvironment,arg)
                .join());
    }

    @Test
    void deleteAnnotationForCourseware_annotationNotFound() {
        UUID annotationId = UUIDs.timeBased();
        AnnotationArg arg = mock(AnnotationArg.class);
        when(arg.getId()).thenReturn(annotationId);

        when(annotationService.findCoursewareAnnotation(annotationId)).thenReturn(Mono.empty());

        annotationSchema
                .deleteAnnotationForCourseware(resolutionEnvironment,arg)
                .handle((annotationArg, throwable) -> {
                    assertEquals(PermissionFault.class, throwable.getClass());
                    return annotationArg;
                })
                .join();
    }

    @Test
    void deleteAnnotationForCourseware_notCreator() {
        UUID annotationId = UUIDs.timeBased();
        AnnotationArg arg = mock(AnnotationArg.class);
        when(arg.getId()).thenReturn(annotationId);

        CoursewareAnnotation annotation = new CoursewareAnnotation() //
                .setId(annotationId) //
                .setCreatorAccountId(UUIDs.timeBased());

        when(annotationService.findCoursewareAnnotation(annotationId)).thenReturn(Mono.just(annotation));

        annotationSchema
                .deleteAnnotationForCourseware(resolutionEnvironment,arg)
                .handle((annotationArg, throwable) -> {
                    assertEquals(PermissionFault.class, throwable.getClass());
                    return annotationArg;
                })
                .join();
    }

    @Test
    void createAnnotationForDeployment_deploymentNotFound() {
        AnnotationArg annotationArg = mock(AnnotationArg.class);

        TestPublisher<DeployedActivity> publisher = TestPublisher.create();
        publisher.error(new DeploymentNotFoundException(elementId, deploymentId));
        when(deploymentService.findDeployment(deploymentId)).thenReturn(publisher.mono());

        PermissionFault e = assertThrows(PermissionFault.class, () ->
                annotationSchema
                        .createAnnotationForDeployment(resolutionEnvironment,deploymentId, elementId, annotationArg)
                        .join());

        assertNotNull(e);
        assertEquals("Unauthorized", e.getMessage());
    }

    @Test
    void createAnnotationForDeployment_noPermission() {
        AnnotationArg annotationArg = mock(AnnotationArg.class);

        when(allowCohortInstructor.test(authenticationContextProvider.get(), cohortId)).thenReturn(false);
        when(allowEnrolledStudent.test(authenticationContextProvider.get(),cohortId)).thenReturn(false);

        PermissionFault e = assertThrows(PermissionFault.class, () ->
                annotationSchema
                        .createAnnotationForDeployment(resolutionEnvironment,deploymentId, elementId, annotationArg)
                        .join());

        assertNotNull(e);
        assertEquals("Unauthorized", e.getMessage());
    }

    @Test
    void createAnnotationForDeployment_invalidBody() {
        AnnotationArg annotationArg = mock(AnnotationArg.class);

        when(annotationArg.getBody()).thenReturn(invalidJsonString);

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationSchema
                        .createAnnotationForDeployment(resolutionEnvironment,deploymentId, elementId, annotationArg)
                        .join());

        assertNotNull(e);
        assertEquals("invalid body json", e.getMessage());
    }

    @Test
    void createAnnotationForDeployment_invalidTarget() {
        AnnotationArg annotationArg = mock(AnnotationArg.class);

        when(annotationArg.getBody()).thenReturn(validJsonString);
        when(annotationArg.getTarget()).thenReturn(invalidJsonString);

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationSchema
                        .createAnnotationForDeployment(resolutionEnvironment,deploymentId, elementId, annotationArg)
                        .join());

        assertNotNull(e);
        assertEquals("invalid target json", e.getMessage());
    }

    @Test
    void createAnnotationForDeployment() {
        AnnotationArg annotationArg = mock(AnnotationArg.class);

        when(annotationArg.getBody()).thenReturn(validJsonString);
        when(annotationArg.getTarget()).thenReturn(validJsonString);
        when(annotationArg.getMotivation()).thenReturn(motivation);

        LearnerAnnotation created = (LearnerAnnotation) annotationSchema
                .createAnnotationForDeployment(resolutionEnvironment,deploymentId, elementId, annotationArg)
                .join();

        assertNotNull(created);
        assertAll(() -> {
            assertNotNull(created.getId());
            assertNotNull(created.getVersion());
            assertEquals(motivation, created.getMotivation());
            assertEquals(elementId, created.getElementId());
            assertNotNull(created.getBodyJson());
            assertEquals(validJsonString, created.getBody());
            assertNotNull(created.getTargetJson());
            assertEquals(validJsonString, created.getTarget());
            assertEquals(accountId, created.getCreatorAccountId());
        });
    }

    @Test
    void getAnnotationsByDeployment_deploymentNotFound() {
        TestPublisher<DeployedActivity> publisher = TestPublisher.create();
        publisher.error(new DeploymentNotFoundException(elementId, deploymentId));
        when(deploymentService.findDeployment(deploymentId)).thenReturn(publisher.mono());

        PermissionFault e = assertThrows(PermissionFault.class,
                () -> annotationSchema
                        .getAnnotationsByDeployment(resolutionEnvironment,deploymentId, motivation, elementId)
                        .join());
        assertNotNull(e);
        assertEquals("Unauthorized", e.getMessage());
    }

    @Test
    void getAnnotationsByDeployment_unauthorized() {
        when(allowCohortInstructor.test(authenticationContextProvider.get(),cohortId)).thenReturn(false);
        when(allowEnrolledStudent.test(authenticationContextProvider.get(),cohortId)).thenReturn(false);

        PermissionFault e = assertThrows(PermissionFault.class,
                () -> annotationSchema
                        .getAnnotationsByDeployment(resolutionEnvironment,deploymentId, Motivation.identifying, elementId)
                        .join());

        assertNotNull(e);
        assertEquals("Unauthorized", e.getMessage());
    }


    @Test
    void getAnnotationsByDeployment_unauthorizedMotivation() {
        when(allowCohortInstructor.test(authenticationContextProvider.get(),cohortId)).thenReturn(false);

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class,
                () -> annotationSchema
                        .getAnnotationsByDeployment(resolutionEnvironment,deploymentId, motivation, elementId)
                        .join());

        assertNotNull(e);
        assertEquals("motivation provided is not permitted", e.getMessage());
    }

    @Test
    void getAnnotationsByDeployment_nullElementId() {
        List<? extends Annotation> found = annotationSchema
                .getAnnotationsByDeployment(resolutionEnvironment,deploymentId, Motivation.identifying, null)
                .join();

        assertNotNull(found);
        assertEquals(1, found.size());

        verify(annotationService, never()).findDeploymentAnnotations(any(UUID.class), any(UUID.class), any(Motivation.class), any(UUID.class));
        verify(annotationService).findDeploymentAnnotations(deploymentId, changeId, Motivation.identifying);
    }

    @Test
    void getAnnotationsByDeployment() {
        List<? extends Annotation> found = annotationSchema
                .getAnnotationsByDeployment(resolutionEnvironment,deploymentId, Motivation.identifying, elementId)
                .join();

        assertNotNull(found);
        assertEquals(1, found.size());

        verify(annotationService).findDeploymentAnnotations(deploymentId, changeId, Motivation.identifying, elementId);
        verify(annotationService, never()).findDeploymentAnnotations(any(UUID.class), any(UUID.class), any(Motivation.class));
    }

    @Test
    void getAnnotationsByDeploymentAccount_deploymentNotFound() {
        UUID accountId = UUID.randomUUID();

        TestPublisher<DeployedActivity> publisher = TestPublisher.create();
        publisher.error(new DeploymentNotFoundException(elementId, deploymentId));
        when(deploymentService.findDeployment(deploymentId)).thenReturn(publisher.mono());

        PermissionFault e = assertThrows(PermissionFault.class,
                () -> annotationSchema
                        .getAnnotationsByDeploymentAccount(resolutionEnvironment,deploymentId, accountId, motivation, elementId)
                        .join());
        assertNotNull(e);
        assertEquals("Unauthorized", e.getMessage());
    }

    @Test
    void getAnnotationsByDeploymentAccount_self() {
        List<? extends Annotation> found = annotationSchema
                .getAnnotationsByDeploymentAccount(resolutionEnvironment,deploymentId, accountId, motivation, elementId)
                .join();

        assertNotNull(found);
        assertEquals(1, found.size());

        verify(annotationService).findLearnerAnnotation(deploymentId, accountId, motivation, elementId);
        verify(annotationService, never()).findLearnerAnnotation(any(UUID.class), any(UUID.class), any(Motivation.class));
    }

    @Test
    void getAnnotationsByDeploymentAccount_self_nullElement() {
        List<? extends Annotation> found = annotationSchema
                .getAnnotationsByDeploymentAccount(resolutionEnvironment,deploymentId, accountId, motivation, null)
                .join();

        assertNotNull(found);
        assertEquals(1, found.size());

        verify(annotationService, never()).findLearnerAnnotation(any(UUID.class), any(UUID.class), any(Motivation.class), any(UUID.class));
        verify(annotationService).findLearnerAnnotation(deploymentId, accountId, motivation);
    }

    @Test
    void getAnnotationsByDeploymentAccount_unauthorized() {
        UUID anotherAccountId = UUID.randomUUID();

        when(allowCohortInstructor.test(authenticationContextProvider.get(),cohortId)).thenReturn(false);

        PermissionFault e = assertThrows(PermissionFault.class,
                () -> annotationSchema
                        .getAnnotationsByDeploymentAccount(resolutionEnvironment,deploymentId, anotherAccountId, motivation, elementId)
                        .join());

        assertNotNull(e);
        assertEquals("Unauthorized", e.getMessage());
    }

    @Test
    void getAnnotationsByDeploymentAccount_nullElementId() {
        UUID anotherAccountId = UUID.randomUUID();
        List<? extends Annotation> found = annotationSchema
                .getAnnotationsByDeploymentAccount(resolutionEnvironment,deploymentId, anotherAccountId, motivation, null)
                .join();

        assertNotNull(found);
        assertEquals(1, found.size());

        verify(annotationService, never()).findLearnerAnnotation(any(UUID.class), any(UUID.class), any(Motivation.class), any(UUID.class));
        verify(annotationService).findLearnerAnnotation(deploymentId, anotherAccountId, motivation);
    }

    @Test
    void getAnnotationsByDeploymentAccount() {
        UUID anotherAccountId = UUID.randomUUID();

        List<? extends Annotation> found = annotationSchema
                .getAnnotationsByDeploymentAccount(resolutionEnvironment,deploymentId, anotherAccountId, motivation, elementId)
                .join();

        assertNotNull(found);
        assertEquals(1, found.size());

        verify(annotationService).findLearnerAnnotation(deploymentId, anotherAccountId, motivation, elementId);
        verify(annotationService, never()).findLearnerAnnotation(any(UUID.class), any(UUID.class),
                any(Motivation.class));
    }

    @Test
    void updateAnnotationForDeployment() {
        UUID annotationId = UUIDs.timeBased();
        AnnotationArg arg = mock(AnnotationArg.class);
        when(arg.getId()).thenReturn(annotationId);
        when(arg.getBody()).thenReturn(validJsonString);
        when(arg.getTarget()).thenReturn(validJsonString);

        LearnerAnnotation original = new LearnerAnnotation() //
                .setId(annotationId)
                .setVersion(UUIDs.timeBased())
                .setMotivation(motivation)
                .setDeploymentId(deploymentId)
                .setElementId(elementId)
                .setCreatorAccountId(UUIDs.timeBased());

        when(annotationService.findLearnerAnnotation(annotationId)).thenReturn(Mono.just(original));

        // run the method
        annotationSchema
                .updateAnnotationForDeployment(resolutionEnvironment,arg)
                .join();

        ArgumentCaptor<LearnerAnnotation> argument = ArgumentCaptor.forClass(LearnerAnnotation.class);
        verify(annotationService).create(argument.capture());

        assertAll(() -> {
            LearnerAnnotation onward = argument.getValue();
            // these become set
            assertNotNull(onward.getBodyJson());
            assertNotNull(onward.getTargetJson());

            assertNotEquals(original.getVersion(), onward.getVersion()); // new version should be installed

            // ensure these are copied over.
            assertEquals(original.getId(), onward.getId());
            assertEquals(original.getMotivation(), onward.getMotivation());
            assertEquals(original.getDeploymentId(), onward.getDeploymentId());
            assertEquals(original.getElementId(), onward.getElementId());
            assertEquals(original.getCreatorAccountId(), onward.getCreatorAccountId());

            assertNotEquals(original, onward);
        });
    }

    @Test
    void updateAnnotationForDeployment_missingAnnotationArgId() {
        AnnotationArg arg = mock(AnnotationArg.class);
        when(arg.getId()).thenReturn(null);

        assertThrows(IllegalArgumentFault.class, () -> annotationSchema
                .updateAnnotationForDeployment(resolutionEnvironment,arg)
                .join());
    }

    @Test
    void updateAnnotationForDeployment_annotationNotFound() {
        UUID annotationId = UUIDs.timeBased();
        AnnotationArg arg = mock(AnnotationArg.class);
        when(arg.getId()).thenReturn(annotationId);

        when(annotationService.findLearnerAnnotation(annotationId)).thenReturn(Mono.empty());

        annotationSchema
                .updateAnnotationForDeployment(resolutionEnvironment,arg)
                .handle((learnerAnnotation, throwable) -> {
                    assertEquals(PermissionFault.class, throwable.getClass());
                    return learnerAnnotation;
                })
                .join();
    }

    @Test
    void deleteAnnotationForDeployment() {
        UUID annotationId = UUIDs.timeBased();
        AnnotationArg arg = mock(AnnotationArg.class);
        when(arg.getId()).thenReturn(annotationId);

        LearnerAnnotation annotation = new LearnerAnnotation() //
                .setId(annotationId) //
                .setCreatorAccountId(accountId);

        when(annotationService.findLearnerAnnotation(annotationId)).thenReturn(Mono.just(annotation));
        when(annotationService.deleteAnnotation(annotation)).thenReturn(Flux.empty());

        annotationSchema
                .deleteAnnotationForDeployment(resolutionEnvironment,arg)
                .join();

        verify(annotationService).deleteAnnotation(eq(annotation));
    }

    @Test
    void deleteAnnotationForDeployment_noIdSpecified() {
        AnnotationArg arg = mock(AnnotationArg.class);
        when(arg.getId()).thenReturn(null);

        assertThrows(IllegalArgumentFault.class, () -> annotationSchema
                .deleteAnnotationForDeployment(resolutionEnvironment,arg)
                .join());
    }

    @Test
    void deleteAnnotationForDeployment_annotationNotFound() {
        UUID annotationId = UUIDs.timeBased();
        AnnotationArg arg = mock(AnnotationArg.class);
        when(arg.getId()).thenReturn(annotationId);

        when(annotationService.findLearnerAnnotation(annotationId)).thenReturn(Mono.empty());

        annotationSchema
                .deleteAnnotationForDeployment(resolutionEnvironment,arg)
                .handle((annotationArg, throwable) -> {
                    assertEquals(PermissionFault.class, throwable.getClass());
                    return annotationArg;
                })
                .join();
    }

    @Test
    void deleteAnnotationForDeployment_notCreatorAndBadDeployment() {
        UUID annotationId = UUIDs.timeBased();
        AnnotationArg arg = mock(AnnotationArg.class);
        when(arg.getId()).thenReturn(annotationId);

        LearnerAnnotation annotation = new LearnerAnnotation() //
                .setId(annotationId) //
                .setDeploymentId(UUIDs.timeBased()) //
                .setCreatorAccountId(UUIDs.timeBased());

        when(annotationService.findLearnerAnnotation(annotationId)).thenReturn(Mono.just(annotation));
        when(deploymentService.findDeployment(any(UUID.class))).thenThrow(new DeploymentNotFoundException(null, null));

        annotationSchema
                .deleteAnnotationForDeployment(resolutionEnvironment, arg)
                .handle((annotationArg, throwable) -> {
                    assertEquals(PermissionFault.class, throwable.getClass());
                    return annotationArg;
                })
                .join();
    }

    @Test
    void deleteAnnotationForDeployment_notCreatorAndNoCohortPermission() {
        UUID annotationId = UUIDs.timeBased();
        AnnotationArg arg = mock(AnnotationArg.class);
        when(arg.getId()).thenReturn(annotationId);

        LearnerAnnotation annotation = new LearnerAnnotation() //
                .setId(annotationId) //
                .setDeploymentId(deploymentId) //
                .setCreatorAccountId(UUIDs.timeBased());

        when(annotationService.findLearnerAnnotation(annotationId)).thenReturn(Mono.just(annotation));
        when(allowCohortInstructor.test(any(AuthenticationContext.class),any(UUID.class))).thenReturn(false);

        annotationSchema
                .deleteAnnotationForDeployment(resolutionEnvironment, arg)
                .handle((annotationArg, throwable) -> {
                    assertEquals(PermissionFault.class, throwable.getClass());
                    return annotationArg;
                })
                .join();
    }

    @Test
    void getCreator_noSelf() {
        Annotation annotation = mock(LearnerAnnotation.class);

        when(annotation.getCreatorAccountId()).thenReturn(accountId);

        Account account = annotationSchema.getCreator(resolutionEnvironment,annotation).join();

        assertNotNull(account);

        verify(accountService).findById(accountId);
    }

    @Test
    void getCreator_deploymentNotFound() {
        LearnerAnnotation annotation = mock(LearnerAnnotation.class);
        when(annotation.getDeploymentId()).thenReturn(deploymentId);
        when(annotation.getCreatorAccountId()).thenReturn(UUID.randomUUID());

        TestPublisher<DeployedActivity> publisher = TestPublisher.create();
        publisher.error(new DeploymentNotFoundException(elementId, deploymentId));
        when(deploymentService.findDeployment(deploymentId)).thenReturn(publisher.mono());

    }

    @Test
    void getCreator_notAuthorized() {
        LearnerAnnotation annotation = mock(LearnerAnnotation.class);
        when(annotation.getDeploymentId()).thenReturn(deploymentId);
        when(annotation.getCreatorAccountId()).thenReturn(UUID.randomUUID());
        when(allowCohortInstructor.test(authenticationContextProvider.get(),cohortId)).thenReturn(false);

        PermissionFault e = assertThrows(PermissionFault.class, () -> annotationSchema
                .getCreator(resolutionEnvironment,annotation)
                .join());

        assertNotNull(e);
        assertEquals("Unauthorized", e.getMessage());

        verify(accountService, never()).findById(any(UUID.class));
    }

    @Test
    void getCreator_noInspection_notAuthorized() {
        CoursewareAnnotation annotation = mock(CoursewareAnnotation.class);
        when(annotation.getCreatorAccountId()).thenReturn(UUID.randomUUID());
        when(annotation.getRootElementId()).thenReturn(rootElementId);

        when(allowWorkspaceReviewerOrHigher.test(any(AuthenticationContext.class),any(UUID.class))).thenReturn(false);

        PermissionFault e = assertThrows(PermissionFault.class, () -> annotationSchema
                .getCreator(resolutionEnvironment,annotation)
                .join());

        assertNotNull(e);
        assertEquals("Unauthorized", e.getMessage());

        verify(accountService, never()).findById(any(UUID.class));
    }

    @Test
    void getCreator() {
        LearnerAnnotation annotation = mock(LearnerAnnotation.class);
        when(annotation.getDeploymentId()).thenReturn(deploymentId);
        when(annotation.getCreatorAccountId()).thenReturn(UUID.randomUUID());

        Account account = annotationSchema.getCreator(resolutionEnvironment,annotation).join();

        assertNotNull(account);

        verify(accountService).findById(any(UUID.class));
    }

    @Test
    void createAnnotationForCourseware_withIdentifyingMotivation_CONTRIBUTOR() {
        when(coursewareElementContributorOrHigher.test(any(),any(), any())).thenReturn(true);
        AnnotationArg annotationArg = mock(AnnotationArg.class);

        when(annotationArg.getBody()).thenReturn(validJsonString);
        when(annotationArg.getTarget()).thenReturn(validJsonString);
        when(annotationArg.getMotivation()).thenReturn(identifyingMotivation);

        CoursewareAnnotation created = (CoursewareAnnotation) annotationSchema
                .createAnnotationForCourseware(resolutionEnvironment,rootElementId, elementId, annotationArg)
                .join();

        assertNotNull(created);
        assertAll(() -> {
            assertNotNull(created.getId());
            assertNotNull(created.getVersion());
            assertEquals(identifyingMotivation, created.getMotivation());
            assertEquals(rootElementId, created.getRootElementId());
            assertEquals(elementId, created.getElementId());
            assertNotNull(created.getBodyJson());
            assertEquals(validJsonString, created.getBody());
            assertNotNull(created.getTargetJson());
            assertEquals(validJsonString, created.getTarget());
            assertEquals(accountId, created.getCreatorAccountId());
        });
        verify(coursewareElementContributorOrHigher).test(any(),any(), any());
        verify(annotationService).create(any(CoursewareAnnotation.class));

    }

    @Test
    void createAnnotationForCourseware_ERROR_withIdentifyingMotivation_REVIEWER() {
        when(coursewareElementContributorOrHigher.test(any(),any(), any())).thenReturn(false);
        AnnotationArg annotationArg = mock(AnnotationArg.class);

        when(annotationArg.getBody()).thenReturn(validJsonString);
        when(annotationArg.getTarget()).thenReturn(validJsonString);
        when(annotationArg.getMotivation()).thenReturn(identifyingMotivation);
        PermissionFault permissionFault = assertThrows(PermissionFault.class, () -> annotationSchema
                .createAnnotationForCourseware(resolutionEnvironment,rootElementId, elementId, annotationArg)
                .join());
        assertNotNull(permissionFault);
        assertEquals("Unauthorized", permissionFault.getMessage());

    }

    @Test
    void updateAnnotationForCourseware_withIdentifyingMotivation_CONTRIBUTOR() {
        UUID annotationId = UUIDs.timeBased();
        AnnotationArg arg = mock(AnnotationArg.class);
        when(arg.getId()).thenReturn(annotationId);
        when(arg.getBody()).thenReturn(validJsonString);
        when(arg.getTarget()).thenReturn(validJsonString);
        when(arg.getMotivation()).thenReturn(identifyingMotivation);

        CoursewareAnnotation original = new CoursewareAnnotation() //
                .setId(annotationId)
                .setVersion(UUIDs.timeBased())
                .setMotivation(identifyingMotivation)
                .setRootElementId(rootElementId)
                .setElementId(elementId)
                .setCreatorAccountId(UUIDs.timeBased());

        when(coursewareElementContributorOrHigher.test(any(),any(), any())).thenReturn(true);
        when(annotationService.findCoursewareAnnotation(annotationId)).thenReturn(Mono.just(original));
        when(allowWorkspaceReviewerOrHigher.test(any(),any(UUID.class))).thenReturn(true);

        // run the method
        annotationSchema.updateAnnotationForCourseware(resolutionEnvironment,arg).join();

        ArgumentCaptor<CoursewareAnnotation> argument = ArgumentCaptor.forClass(CoursewareAnnotation.class);
        verify(annotationService).create(argument.capture());

        assertAll(() -> {
            CoursewareAnnotation onward = argument.getValue();
            // these become set
            assertNotNull(onward.getBodyJson());
            assertNotNull(onward.getTargetJson());

            assertNotEquals(original.getVersion(), onward.getVersion()); // new version should be installed

            // ensure these are copied over.
            assertEquals(original.getId(), onward.getId());
            assertEquals(original.getMotivation(), onward.getMotivation());
            assertEquals(original.getRootElementId(), onward.getRootElementId());
            assertEquals(original.getElementId(), onward.getElementId());
            assertEquals(original.getCreatorAccountId(), onward.getCreatorAccountId());

            assertNotEquals(original, onward);
        });
    }

    @Test
    void deleteAnnotationForCourseware_withIdentifyingMotivation_CONTRIBUTOR() {
        UUID annotationId = UUIDs.timeBased();
        when(coursewareElementContributorOrHigher.test(any(),any(), any())).thenReturn(true);
        AnnotationArg arg = mock(AnnotationArg.class);
        when(arg.getId()).thenReturn(annotationId);

        CoursewareAnnotation annotation = new CoursewareAnnotation() //
                .setId(annotationId) //
                .setCreatorAccountId(UUIDs.timeBased())
                .setMotivation(Motivation.identifying);

        when(annotationService.findCoursewareAnnotation(annotationId)).thenReturn(Mono.just(annotation));
        when(annotationService.deleteAnnotation(annotation)).thenReturn(Flux.empty());
        AnnotationArg annotationArg = annotationSchema.deleteAnnotationForCourseware(resolutionEnvironment,arg).join();
        verify(annotationService).deleteAnnotation(eq(annotation));
        assertNotNull(annotationArg);

    }

    @Test
    void deleteAnnotationForCourseware_ERROR_withIdentifyingMotivation_REVIWER() {
        UUID annotationId = UUIDs.timeBased();
        when(coursewareElementContributorOrHigher.test(any(),any(), any())).thenReturn(false);
        AnnotationArg arg = mock(AnnotationArg.class);
        when(arg.getId()).thenReturn(annotationId);

        CoursewareAnnotation annotation = new CoursewareAnnotation() //
                .setId(annotationId) //
                .setCreatorAccountId(UUIDs.timeBased())
                .setMotivation(Motivation.identifying);

        when(annotationService.findCoursewareAnnotation(annotationId)).thenReturn(Mono.just(annotation));
        annotationSchema
                .deleteAnnotationForCourseware(resolutionEnvironment,arg)
                .handle((annotationArg, throwable) -> {
                    assertEquals(PermissionFault.class, throwable.getClass());
                    assertEquals("Unauthorized", throwable.getMessage());
                    return annotationArg;
                })
                .join();
    }

}
