package com.smartsparrow.annotation.service;

import static com.smartsparrow.annotation.service.Motivation.commenting;
import static com.smartsparrow.annotation.service.Motivation.replying;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.annotation.data.AnnotationGateway;
import com.smartsparrow.annotation.lang.AnnotationAlreadyExistsFault;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.util.Json;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AnnotationServiceTest {

    @InjectMocks
    private AnnotationService annotationService;

    @Mock
    private AnnotationGateway annotationGateway;

    private static final UUID changeId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final Motivation motivation = Motivation.identifying;
    private static final UUID annotationId = UUIDs.timeBased();
    private static final UUID creatorId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final String body = "{\n\t\"body\": \"body\"\n}";
    private static final String target = "{\n\t\"target\": \"target\"\n}";
    private static final String invalidJsonString = "invalid";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(annotationGateway.persist(any(CoursewareAnnotation.class))).thenReturn(Flux.just(new Void[]{}));
        when(annotationGateway.persist(any(LearnerAnnotation.class))).thenReturn(Flux.just(new Void[]{}));

        when(annotationGateway.findCoursewareAnnotation(any(UUID.class))).thenReturn(Mono.just(new CoursewareAnnotation()));
        when(annotationGateway.findCoursewareAnnotation(any(UUID.class), any(Motivation.class)))
                .thenReturn(Flux.just(new CoursewareAnnotation()));
        when(annotationGateway.findCoursewareAnnotation(any(UUID.class), any(UUID.class), any(Motivation.class)))
                .thenReturn(Flux.just(new CoursewareAnnotation()));

        when(annotationGateway.findLearnerAnnotation(any(UUID.class))).thenReturn(Mono.just(new LearnerAnnotation()));
        when(annotationGateway.findLearnerAnnotation(any(UUID.class), any(UUID.class), any(Motivation.class)))
                .thenReturn(Flux.just(new LearnerAnnotation()));
        when(annotationGateway.findLearnerAnnotation(any(UUID.class), any(UUID.class), any(Motivation.class), any(UUID.class)))
                .thenReturn(Flux.just(new LearnerAnnotation()));
        when(annotationGateway.findDeploymentAnnotations(any(UUID.class), any(UUID.class), any(Motivation.class)))
                .thenReturn(Flux.just(new DeploymentAnnotation(new LearnerAnnotation(), changeId)));
        when(annotationGateway.findDeploymentAnnotations(any(UUID.class), any(UUID.class), any(Motivation.class), any(UUID.class)))
                .thenReturn(Flux.just(new DeploymentAnnotation(new LearnerAnnotation(), changeId)));
        when(annotationGateway.findAnnotationRead(any(CoursewareAnnotation.class), any(UUID.class)))
                .thenReturn(Mono.just(new CoursewareAnnotationReadByUser()));

        when(annotationGateway.deleteAnnotationByRootElementId(any(UUID.class))).thenReturn(Flux.just(new Void[]{}));
        when(annotationGateway.resolveComments(any(CoursewareAnnotationKey.class), anyBoolean())).thenReturn(Flux.just(new Void[]{}));
        when(annotationGateway.readComments(any(UUID.class), any(UUID.class), any(UUID.class), any(UUID.class))).thenReturn(Flux.just(new Void[]{}));
        when(annotationGateway.unreadComments(any(UUID.class), any(UUID.class), any(UUID.class), any(UUID.class))).thenReturn(Flux.just(new Void[]{}));
    }

    @Test
    void create_nullAnnotation() {
        CoursewareAnnotation annotation = null;
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> annotationService.create(annotation).blockLast());

        assertNotNull(e);
        assertEquals("missing annotation", e.getMessage());
    }

    @Test
    void create_nullRootElement() {
        CoursewareAnnotation annotation = new CoursewareAnnotation();
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> annotationService.create(annotation).blockLast());

        assertNotNull(e);
        assertEquals("missing root element id", e.getMessage());
    }

    @Test
    void create_nullCreator() {
        CoursewareAnnotation annotation = new CoursewareAnnotation()
                .setRootElementId(UUID.randomUUID());
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> annotationService.create(annotation).blockLast());

        assertNotNull(e);
        assertEquals("missing annotation creator account id", e.getMessage());
    }

    @Test
    void create_nullMotivation() {
        CoursewareAnnotation annotation = new CoursewareAnnotation()
                .setRootElementId(UUID.randomUUID())
                .setCreatorAccountId(UUID.randomUUID());
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> annotationService.create(annotation).blockLast());

        assertNotNull(e);
        assertEquals("missing annotation motivation", e.getMessage());
    }

    @Test
    void create() {
        CoursewareAnnotation annotation = new CoursewareAnnotation()
                .setRootElementId(UUID.randomUUID())
                .setCreatorAccountId(UUID.randomUUID())
                .setMotivation(commenting);

        annotationService.create(annotation).blockLast();

        verify(annotationGateway).persist(annotation);
    }

    @Test
    void create_with_annotationId() {
        CoursewareAnnotation annotation = new CoursewareAnnotation()
                .setRootElementId(UUID.randomUUID())
                .setCreatorAccountId(UUID.randomUUID())
                .setMotivation(commenting);

        when(annotationGateway.findCoursewareAnnotation(annotationId)).thenReturn(Mono.empty());

        annotationService.create(annotation, annotationId).blockLast();

        verify(annotationGateway).findCoursewareAnnotation(annotationId);
        verify(annotationGateway).persist(annotation);
    }

    @Test
    void create_with_annotationId_conflict() {
        CoursewareAnnotation annotation = new CoursewareAnnotation()
                .setRootElementId(UUID.randomUUID())
                .setCreatorAccountId(UUID.randomUUID())
                .setMotivation(commenting);

        when(annotationGateway.findCoursewareAnnotation(annotationId)).thenReturn(Mono.just(annotation));

        assertThrows(AnnotationAlreadyExistsFault.class, () -> annotationService.create(annotation, annotationId).blockLast());

        verify(annotationGateway).findCoursewareAnnotation(annotationId);
        verify(annotationGateway, never()).persist(annotation);
    }

    @Test
    void findCoursewareAnnotation_nullId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class,
                () -> annotationService.findCoursewareAnnotation(null).block());

        assertNotNull(e);
        assertEquals("missing annotation id", e.getMessage());
    }

    @Test
    void findCoursewareAnnotation() {
        UUID annotationId = UUID.randomUUID();
        CoursewareAnnotation found = annotationService.findCoursewareAnnotation(annotationId).block();

        assertNotNull(found);
        verify(annotationGateway).findCoursewareAnnotation(annotationId);
    }

    @Test
    void findCoursewareAnnotationByMotivation_nullId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.findCoursewareAnnotation(null, commenting, accountId));

        assertNotNull(e);
        assertEquals("missing root element id", e.getMessage());
    }

    @Test
    void findCoursewareAnnotationByMotivation_nullMotivation() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.findCoursewareAnnotation(UUID.randomUUID(), null, accountId));

        assertNotNull(e);
        assertEquals("missing motivation", e.getMessage());
    }

    @Test
    void findCoursewareAnnotationByMotivation_nullAccount() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.findCoursewareAnnotation(UUID.randomUUID(), commenting, null));

        assertNotNull(e);
        assertEquals("missing accountId", e.getMessage());
    }

    @Test
    void findCoursewareAnnotationByMotivation() {
        UUID rootElementId = UUID.randomUUID();

        List<CoursewareAnnotationPayload> found = annotationService
                .findCoursewareAnnotation(rootElementId, commenting, accountId).collectList().block();

        assertNotNull(found);
        assertEquals(1, found.size());

        verify(annotationGateway).findCoursewareAnnotation(rootElementId, commenting);
    }

    @Test
    void findCoursewareAnnotationByElement_nullRootId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.findCoursewareAnnotation(null, null, commenting));

        assertNotNull(e);
        assertEquals("missing root element id", e.getMessage());
        verify(annotationGateway, never()).findCoursewareAnnotation(any(UUID.class), any(UUID.class), any(Motivation.class));
    }

    @Test
    void findCoursewareAnnotationByElement_nullElementId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.findCoursewareAnnotation(UUID.randomUUID(), null, commenting));

        assertNotNull(e);
        assertEquals("missing element id", e.getMessage());
        verify(annotationGateway, never()).findCoursewareAnnotation(any(UUID.class), any(UUID.class), any(Motivation.class));
    }

    @Test
    void findCoursewareAnnotationByElement_nullMotivation() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.findCoursewareAnnotation(UUID.randomUUID(), UUID.randomUUID(), null));

        assertNotNull(e);
        assertEquals("missing motivation", e.getMessage());
        verify(annotationGateway, never()).findCoursewareAnnotation(any(UUID.class), any(UUID.class), any(Motivation.class));
    }

    @Test
    void findCoursewareAnnotationByElement() {
        UUID rootElementId = UUID.randomUUID();
        UUID elementId = UUID.randomUUID();

        List<CoursewareAnnotation> found = annotationService
                .findCoursewareAnnotation(rootElementId, elementId, Motivation.bookmarking).collectList().block();

        assertNotNull(found);
        assertEquals(1, found.size());
        verify(annotationGateway).findCoursewareAnnotation(rootElementId, elementId, Motivation.bookmarking);
    }

    @Test
    void createLearnerAnnotation_nullAnnotation() {
        LearnerAnnotation annotation = null;

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.create(annotation).blockLast());

        assertNotNull(e);
        assertEquals("missing annotation", e.getMessage());
        verify(annotationGateway, never()).persist(any(LearnerAnnotation.class));
    }

    @Test
    void createLearnerAnnotation_nullDeploymentId() {
        LearnerAnnotation annotation = new LearnerAnnotation();

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.create(annotation).blockLast());

        assertNotNull(e);
        assertEquals("missing annotation deployment id", e.getMessage());
        verify(annotationGateway, never()).persist(any(LearnerAnnotation.class));
    }

    @Test
    void createLearnerAnnotation_nullCreatorId() {
        LearnerAnnotation annotation = new LearnerAnnotation()
                .setDeploymentId(UUID.randomUUID());

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.create(annotation).blockLast());

        assertNotNull(e);
        assertEquals("missing annotation creator account id", e.getMessage());
        verify(annotationGateway, never()).persist(any(LearnerAnnotation.class));
    }

    @Test
    void createLearnerAnnotation_nullMotivation() {
        LearnerAnnotation annotation = new LearnerAnnotation()
                .setDeploymentId(UUID.randomUUID())
                .setCreatorAccountId(UUID.randomUUID());

        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.create(annotation).blockLast());

        assertNotNull(e);
        assertEquals("missing annotation motivation", e.getMessage());
        verify(annotationGateway, never()).persist(any(LearnerAnnotation.class));
    }

    @Test
    void createLearnerAnnotation() {
        LearnerAnnotation annotation = new LearnerAnnotation()
                .setDeploymentId(UUID.randomUUID())
                .setCreatorAccountId(UUID.randomUUID())
                .setMotivation(Motivation.highlighting);

        annotationService.create(annotation).blockLast();

        verify(annotationGateway).persist(any(LearnerAnnotation.class));
    }

    @Test
    void findLearnerAnnotation_nullId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.findLearnerAnnotation(null));

        assertNotNull(e);
        assertEquals("missing annotation id", e.getMessage());
        verify(annotationGateway, never()).findLearnerAnnotation(any(UUID.class));
    }

    @Test
    void findLearnerAnnotation() {
        UUID annotationId = UUID.randomUUID();

        LearnerAnnotation found = annotationService.findLearnerAnnotation(annotationId).block();

        assertNotNull(found);
        verify(annotationGateway).findLearnerAnnotation(annotationId);
    }

    @Test
    void findLearnerAnnotationByMotivation_nullDeploymentId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.findLearnerAnnotation(null, UUID.randomUUID(), null));

        assertNotNull(e);
        assertEquals("missing deployment id", e.getMessage());
        verify(annotationGateway, never()).findLearnerAnnotation(any(UUID.class), any(UUID.class), any(Motivation.class));
    }

    @Test
    void findLearnerAnnotationByMotivation_nullCreatorId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.findLearnerAnnotation(UUID.randomUUID(), null, Motivation.classifying));

        assertNotNull(e);
        assertEquals("missing creator account id", e.getMessage());
        verify(annotationGateway, never()).findLearnerAnnotation(any(UUID.class), any(UUID.class), any(Motivation.class));
    }

    @Test
    void findLearnerAnnotationByMotivation_nullMotivation() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.findLearnerAnnotation(UUID.randomUUID(), UUID.randomUUID(), null));

        assertNotNull(e);
        assertEquals("missing motivation", e.getMessage());
        verify(annotationGateway, never()).findLearnerAnnotation(any(UUID.class), any(UUID.class), any(Motivation.class));
    }

    @Test
    void findLearnerAnnotationByMotivation() {
        List<LearnerAnnotation> found = annotationService
                .findLearnerAnnotation(UUID.randomUUID(), UUID.randomUUID(), Motivation.highlighting).collectList().block();

        assertNotNull(found);
        assertEquals(1, found.size());
        verify(annotationGateway).findLearnerAnnotation(any(UUID.class), any(UUID.class), any(Motivation.class));
    }

    @Test
    void findLearnerAnnotationByElement_nullDeploymentId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.findLearnerAnnotation(null, null, null, null));

        assertNotNull(e);
        assertEquals("missing deployment id", e.getMessage());
        verify(annotationGateway, never()).findLearnerAnnotation(any(UUID.class), any(UUID.class), any(Motivation.class), any(UUID.class));
    }

    @Test
    void findLearnerAnnotationByElement_nullCreatorId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.findLearnerAnnotation(UUID.randomUUID(), null, null, null));

        assertNotNull(e);
        assertEquals("missing creator account id", e.getMessage());
        verify(annotationGateway, never()).findLearnerAnnotation(any(UUID.class), any(UUID.class), any(Motivation.class), any(UUID.class));
    }

    @Test
    void findLearnerAnnotationByElement_nullMotivation() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.findLearnerAnnotation(UUID.randomUUID(), UUID.randomUUID(), null, null));

        assertNotNull(e);
        assertEquals("missing motivation", e.getMessage());
        verify(annotationGateway, never()).findLearnerAnnotation(any(UUID.class), any(UUID.class), any(Motivation.class), any(UUID.class));
    }

    @Test
    void findLearnerAnnotationByElement_nullElementId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.findLearnerAnnotation(UUID.randomUUID(), UUID.randomUUID(), Motivation.bookmarking, null));

        assertNotNull(e);
        assertEquals("missing element id", e.getMessage());
        verify(annotationGateway, never()).findLearnerAnnotation(any(UUID.class), any(UUID.class), any(Motivation.class), any(UUID.class));
    }

    @Test
    void findLearnerAnnotationByElement() {
        UUID deploymentId = UUID.randomUUID();
        UUID creatorAccountId = UUID.randomUUID();
        UUID elementId = UUID.randomUUID();

        List<LearnerAnnotation> found = annotationService
                .findLearnerAnnotation(deploymentId, creatorAccountId, commenting, elementId).collectList().block();

        assertNotNull(found);
        assertEquals(1, found.size());
        verify(annotationGateway).findLearnerAnnotation(deploymentId, creatorAccountId, commenting, elementId);
    }

    @Test
    void findDeploymentAnnotations_nullDeploymentId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.findDeploymentAnnotations(null, null, null));

        assertNotNull(e);
        assertEquals("deploymentId is required", e.getMessage());
        verify(annotationGateway, never()).findLearnerAnnotation(any(UUID.class), any(UUID.class), any(Motivation.class));
    }

    @Test
    void findDeploymentAnnotations_nullChangeId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.findDeploymentAnnotations(UUID.randomUUID(), null, null));

        assertNotNull(e);
        assertEquals("changeId is required", e.getMessage());
        verify(annotationGateway, never()).findDeploymentAnnotations(any(UUID.class), any(UUID.class), any(Motivation.class));
    }

    @Test
    void findDeploymentAnnotations_nullMotivation() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.findDeploymentAnnotations(UUID.randomUUID(),UUID.randomUUID(), null));

        assertNotNull(e);
        assertEquals("motivation is required", e.getMessage());
        verify(annotationGateway, never()).findDeploymentAnnotations(any(UUID.class), any(UUID.class), any(Motivation.class));
    }

    @Test
    void findDeploymentAnnotations() {
        List<DeploymentAnnotation> found = annotationService
                .findDeploymentAnnotations(UUID.randomUUID(), UUID.randomUUID(), Motivation.highlighting)
                .collectList()
                .block();

        assertNotNull(found);
        assertEquals(1, found.size());
        verify(annotationGateway).findDeploymentAnnotations(any(UUID.class), any(UUID.class), any(Motivation.class));
    }

    @Test
    void findDeploymentAnnotationsByElement_nullDeploymentId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.findDeploymentAnnotations(null, UUID.randomUUID(), Motivation.identifying, UUID.randomUUID()));

        assertNotNull(e);
        assertEquals("deploymentId is required", e.getMessage());
        verify(annotationGateway, never()).findDeploymentAnnotations(any(UUID.class), any(UUID.class), any(Motivation.class), any(UUID.class));
    }

    @Test
    void findDeploymentAnnotationsByElement_nullChangeId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.findDeploymentAnnotations(UUID.randomUUID(),null, Motivation.identifying, UUID.randomUUID()));

        assertNotNull(e);
        assertEquals("changeId is required", e.getMessage());
        verify(annotationGateway, never()).findDeploymentAnnotations(any(UUID.class), any(UUID.class), any(Motivation.class), any(UUID.class));
    }

    @Test
    void findDeploymentAnnotationsByElement_nullMotivation() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.findDeploymentAnnotations(UUID.randomUUID(), UUID.randomUUID(), null, UUID.randomUUID()));

        assertNotNull(e);
        assertEquals("motivation is required", e.getMessage());
        verify(annotationGateway, never()).findDeploymentAnnotations(any(UUID.class), any(UUID.class), any(Motivation.class), any(UUID.class));
    }

    @Test
    void findDeploymentAnnotationsByElement_nullElementId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.findDeploymentAnnotations(UUID.randomUUID(), UUID.randomUUID(), Motivation.bookmarking, null));

        assertNotNull(e);
        assertEquals("elementId is required", e.getMessage());
        verify(annotationGateway, never()).findDeploymentAnnotations(any(UUID.class), any(UUID.class), any(Motivation.class), any(UUID.class));
    }

    @Test
    void findDeploymentAnnotationsByElement() {
        UUID deploymentId = UUID.randomUUID();
        UUID elementId = UUID.randomUUID();

        List<DeploymentAnnotation> found = annotationService
                .findDeploymentAnnotations(deploymentId, changeId, commenting, elementId)
                .collectList()
                .block();

        assertNotNull(found);
        assertEquals(1, found.size());
        verify(annotationGateway).findDeploymentAnnotations(deploymentId, changeId, commenting, elementId);
    }

    @Test
    void updateCoursewareAnnotation_success() {
        CoursewareAnnotation coursewareAnnotation = new CoursewareAnnotation()
                .setId(UUID.randomUUID())
                .setCreatorAccountId(creatorId)
                .setMotivation(motivation)
                .setRootElementId(rootElementId)
                .setBodyJson(Json.toJsonNode(body))
                .setTargetJson(Json.toJsonNode(target))
                .setVersion(UUID.randomUUID())
                .setElementId(elementId);

        when(annotationGateway.findCoursewareAnnotation(annotationId)).thenReturn(Mono.just(coursewareAnnotation));
        when(annotationGateway.persist(coursewareAnnotation)).thenReturn(Flux.just(new Void[]{}));
        CoursewareAnnotation expectedAnnotation = annotationService.updateCoursewareAnnotation(annotationId, body, target).block();
        assertNotNull(expectedAnnotation);
        ArgumentCaptor<CoursewareAnnotation> argument = ArgumentCaptor.forClass(CoursewareAnnotation.class);
        verify(annotationGateway).persist(argument.capture());

        assertAll(() -> {
            CoursewareAnnotation onward = argument.getValue();
            assertNotNull(onward.getBodyJson());
            assertNotNull(onward.getTargetJson());

            assertNotEquals(coursewareAnnotation.getVersion(), onward.getVersion());

            assertEquals(coursewareAnnotation.getId(), onward.getId());
            assertEquals(coursewareAnnotation.getMotivation(), onward.getMotivation());
            assertEquals(coursewareAnnotation.getRootElementId(), onward.getRootElementId());
            assertEquals(coursewareAnnotation.getElementId(), onward.getElementId());
            assertEquals(coursewareAnnotation.getCreatorAccountId(), onward.getCreatorAccountId());

            assertNotEquals(coursewareAnnotation, onward);
        });
    }

    @Test
    void updateCoursewareAnnotation_InvalidAnnotationBody() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.updateCoursewareAnnotation(annotationId, invalidJsonString, target).block());

        assertNotNull(e);
        assertEquals("invalid body json", e.getMessage());
    }

    @Test
    void updateCoursewareAnnotation_InvalidAnnotationTarget() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.updateCoursewareAnnotation(annotationId, body, invalidJsonString).block());

        assertNotNull(e);
        assertEquals("invalid target json", e.getMessage());
    }

    @Test
    void updateLearnerAnnotation_success() {
        LearnerAnnotation learnerAnnotation = new LearnerAnnotation()
                .setId(UUID.randomUUID())
                .setCreatorAccountId(creatorId)
                .setMotivation(motivation)
                .setDeploymentId(deploymentId)
                .setBodyJson(Json.toJsonNode(body))
                .setTargetJson(Json.toJsonNode(target))
                .setVersion(UUID.randomUUID())
                .setElementId(elementId);

        when(annotationGateway.findLearnerAnnotation(annotationId)).thenReturn(Mono.just(learnerAnnotation));
        when(annotationGateway.persist(learnerAnnotation)).thenReturn(Flux.just(new Void[]{}));
        LearnerAnnotation expectedAnnotation = annotationService.updateLearnerAnnotation(annotationId, body, target).block();
        assertNotNull(expectedAnnotation);
        ArgumentCaptor<LearnerAnnotation> argument = ArgumentCaptor.forClass(LearnerAnnotation.class);
        verify(annotationGateway).persist(argument.capture());

        assertAll(() -> {
            LearnerAnnotation onward = argument.getValue();
            assertNotNull(onward.getBodyJson());
            assertNotNull(onward.getTargetJson());

            assertNotEquals(learnerAnnotation.getVersion(), onward.getVersion());

            assertEquals(learnerAnnotation.getId(), onward.getId());
            assertEquals(learnerAnnotation.getMotivation(), onward.getMotivation());
            assertEquals(learnerAnnotation.getDeploymentId(), onward.getDeploymentId());
            assertEquals(learnerAnnotation.getElementId(), onward.getElementId());
            assertEquals(learnerAnnotation.getCreatorAccountId(), onward.getCreatorAccountId());

            assertNotEquals(learnerAnnotation, onward);
        });
    }

    @Test
    void updateLearnerAnnotation_InvalidAnnotationBody() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.updateLearnerAnnotation(annotationId, invalidJsonString, target).block());

        assertNotNull(e);
        assertEquals("invalid body json", e.getMessage());
    }

    @Test
    void updateLearnerAnnotation_InvalidAnnotationTarget() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.updateLearnerAnnotation(annotationId, body, invalidJsonString).block());

        assertNotNull(e);
        assertEquals("invalid target json", e.getMessage());
    }

    @Test
    void deleteCoursewareAnnotationByElementAndParentActivityId() {
        UUID rootElementId = UUID.randomUUID();
        UUID elementId = UUID.randomUUID();

        List<CoursewareAnnotation> found = annotationService
                .findCoursewareAnnotation(rootElementId, elementId, Motivation.identifying).collectList().block();

        assertNotNull(found);
        assertEquals(1, found.size());
        //Delete CoursewareAnnotation internally uses below annotationGateway services hence both are verified
        verify(annotationGateway).findCoursewareAnnotation(rootElementId, elementId, Motivation.identifying);
        verify(annotationGateway, never()).deleteAnnotation(any(CoursewareAnnotation.class));
    }

    @Test
    void deleteCoursewareAnnotationByElementAndParentActivityIdNullRootId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.deleteAnnotation(null, UUID.randomUUID()));

        assertNotNull(e);
        assertEquals("rootActivityId is required", e.getMessage());
    }

    @Test
    void deleteCoursewareAnnotationByElementByRootElementId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.deleteAnnotationByRootElementId(null));

        assertNotNull(e);
        assertEquals("missing rootElementId", e.getMessage());
    }

    @Test
    void deleteCoursewareAnnotationByRootElementId() {
        UUID rootElementId = UUID.randomUUID();

        annotationService.deleteAnnotationByRootElementId(rootElementId);

        verify(annotationGateway).deleteAnnotationByRootElementId(rootElementId);
    }

    @Test
    void deleteCoursewareAnnotationByElementAndInteractiveIdNullRootId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.deleteAnnotation(UUID.randomUUID(), null));

        assertNotNull(e);
        assertEquals("elementId is required", e.getMessage());
        verify(annotationGateway, never()).findCoursewareAnnotation(any(UUID.class), any(UUID.class), any(Motivation.class));
        verify(annotationGateway, never()).deleteAnnotation(any(CoursewareAnnotation.class));
    }

    @Test
    void moveCoursewareAnnotationByOldRootElementIdNull() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.moveAnnotations(null, UUID.randomUUID(), UUID.randomUUID()));

        assertNotNull(e);
        assertEquals("missing old root element id", e.getMessage());
    }

    @Test
    void moveCoursewareAnnotationByNewRootElementIdNull() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.moveAnnotations(UUID.randomUUID(), UUID.randomUUID(), null));

        assertNotNull(e);
        assertEquals("missing new root element id", e.getMessage());
    }

    @Test
    void moveCoursewareAnnotationByElementIdNull() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.moveAnnotations(UUID.randomUUID(), null, UUID.randomUUID()));

        assertNotNull(e);
        assertEquals("missing element id", e.getMessage());
    }

    @Test
    void resolveCommentsNullCoursewareAnnotationKeys() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.resolveComments(null, true));

        assertNotNull(e);
        assertEquals("coursewareAnnotationKeys is required", e.getMessage());
    }

    @Test
    void resolveCommentsNullResolved() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.resolveComments(new ArrayList<>(), null));

        assertNotNull(e);
        assertEquals("resolved is required", e.getMessage());
    }

    @Test
    void resolveComments() {
        CoursewareAnnotationKey annotation1 = new CoursewareAnnotationKey()
                .setId(annotationId)
                .setVersion(UUID.randomUUID());

        CoursewareAnnotationKey annotation2 = new CoursewareAnnotationKey()
                .setId(UUID.randomUUID())
                .setVersion(UUID.randomUUID());
        List<CoursewareAnnotationKey> coursewareAnnotations = new ArrayList<>();
        coursewareAnnotations.add(annotation1);
        coursewareAnnotations.add(annotation2);

        annotationService.resolveComments(coursewareAnnotations, true).collectList().block();

        verify(annotationGateway).resolveComments(annotation1, true);
        verify(annotationGateway).resolveComments(annotation2, true);
    }

    @Test
    void readCommentsNullRootElementId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.readComments(null, elementId, new ArrayList<>(), true, accountId));

        assertNotNull(e);
        assertEquals("rootElementId is required", e.getMessage());
    }

    @Test
    void readCommentsNullElementId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.readComments(rootElementId, null, new ArrayList<>(), true, accountId));

        assertNotNull(e);
        assertEquals("elementId is required", e.getMessage());
    }

    @Test
    void readCommentsNullAnnotationIds() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.readComments(rootElementId, elementId, null, true, accountId));

        assertNotNull(e);
        assertEquals("annotationIds is required", e.getMessage());
    }

    @Test
    void readCommentsNullRead() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.readComments(rootElementId, elementId, new ArrayList<>(), null, accountId));

        assertNotNull(e);
        assertEquals("read is required", e.getMessage());
    }

    @Test
    void readCommentsNullAccountId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () ->
                annotationService.readComments(rootElementId, elementId, new ArrayList<>(), true, null));

        assertNotNull(e);
        assertEquals("userId is required", e.getMessage());
    }

    @Test
    void readComments() {
        UUID annotationId2 = UUID.randomUUID();
        List<UUID> coursewareAnnotations = new ArrayList<>();
        coursewareAnnotations.add(annotationId);
        coursewareAnnotations.add(annotationId2);

        annotationService.readComments(rootElementId, elementId, coursewareAnnotations, true, accountId)
                .collectList()
                .block();

        verify(annotationGateway).readComments(rootElementId, elementId, annotationId, accountId);
        verify(annotationGateway).readComments(rootElementId, elementId, annotationId2, accountId);
    }

    @Test
    void unreadComments() {
        UUID annotationId2 = UUID.randomUUID();
        List<UUID> coursewareAnnotations = new ArrayList<>();
        coursewareAnnotations.add(annotationId);
        coursewareAnnotations.add(annotationId2);

        annotationService.readComments(rootElementId, elementId, coursewareAnnotations, false, accountId)
                .collectList()
                .block();

        verify(annotationGateway).unreadComments(rootElementId, elementId, annotationId, accountId);
        verify(annotationGateway).unreadComments(rootElementId, elementId, annotationId2, accountId);
    }

    @Test
    void aggregateCoursewareAnnotation() {
        UUID annotationId1 = UUIDs.timeBased();
        CoursewareAnnotation annotation1 = new CoursewareAnnotation().setId(annotationId1).setRootElementId(
                rootElementId).setMotivation(
                commenting).setCreatorAccountId(accountId).setResolved(true).setElementId(elementId);
        CoursewareAnnotationReadByUser readByUser1 = new CoursewareAnnotationReadByUser().setRootElementId(rootElementId).setElementId(
                elementId).setUserId(accountId).setAnnotationId(
                annotationId1);

        UUID annotationId2 = UUIDs.timeBased();
        CoursewareAnnotation annotation2 = new CoursewareAnnotation().setId(annotationId2).setRootElementId(
                rootElementId).setMotivation(
                commenting).setCreatorAccountId(accountId).setResolved(false).setElementId(elementId);
        CoursewareAnnotationReadByUser readByUser2 = new CoursewareAnnotationReadByUser().setRootElementId(rootElementId).setElementId(
                elementId).setUserId(accountId).setAnnotationId(
                annotationId2);

        CoursewareAnnotation annotation3 = new CoursewareAnnotation().setId(annotationId).setRootElementId(rootElementId).setMotivation(
                commenting).setCreatorAccountId(accountId).setResolved(false).setElementId(elementId);

        Flux<CoursewareAnnotation> coursewareAnnotationFlux = Flux.just(annotation1, annotation2, annotation3);

        when(annotationGateway.findCoursewareAnnotation(rootElementId, replying)).thenReturn(Flux.empty());
        when(annotationGateway.findCoursewareAnnotation(rootElementId, commenting)).thenReturn(coursewareAnnotationFlux);
        when(annotationGateway.findAnnotationRead(annotation1, accountId)).thenReturn(Mono.just(readByUser1));
        when(annotationGateway.findAnnotationRead(annotation2, accountId)).thenReturn(Mono.just(readByUser2));
        when(annotationGateway.findAnnotationRead(annotation3, accountId)).thenReturn(Mono.empty());

        CoursewareAnnotationAggregate coursewareAnnotationAggregate = annotationService.aggregateCoursewareAnnotation(
                rootElementId,
                null,
                accountId).block();

        assert coursewareAnnotationAggregate != null;

        assertEquals(2, coursewareAnnotationAggregate.getRead());
        assertEquals(1, coursewareAnnotationAggregate.getUnRead());
        assertEquals(1, coursewareAnnotationAggregate.getResolved());
        assertEquals(2, coursewareAnnotationAggregate.getUnResolved());
        assertEquals(3, coursewareAnnotationAggregate.getTotal());
    }
}
