package com.smartsparrow.courseware.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartsparrow.util.Json;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.annotation.data.AnnotationGateway;
import com.smartsparrow.annotation.service.CoursewareAnnotation;
import com.smartsparrow.annotation.service.Motivation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class AnnotationDuplicationServiceTest {

    @InjectMocks
    private AnnotationDuplicationService annotationDuplicationService;

    @Mock
    private AnnotationGateway annotationGateway;

    private static final UUID interactiveId = UUID.randomUUID();
    private static final UUID activityId = UUID.randomUUID();
    private static final UUID annotationId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();

    private final CoursewareAnnotation annotation = new CoursewareAnnotation()
            .setId(annotationId)
            .setElementId(interactiveId)
            .setRootElementId(activityId)
            .setMotivation(Motivation.classifying);


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        JSONObject targetJsonObject = new JSONObject();
        targetJsonObject.put("id", interactiveId.toString());
        targetJsonObject.put("type", "INTERACTIVE");
        JSONArray targetJson = new JSONArray();
        targetJson.put(targetJsonObject);
        annotation.setTargetJson(Json.toJsonNode(targetJson.toString()));

    }

    @Test
    void findIdsByElement() {
        when(annotationGateway.findCoursewareAnnotation(any(), any(), any())).thenReturn(Flux.just(annotation));

        StepVerifier.create(annotationDuplicationService.findIdsByElement(UUIDs.random(), UUIDs.random()))
                .expectNext(annotationId)
                .thenConsumeWhile(x -> true)
                .verifyComplete();
    }

    @Test
    void duplicate() {
        UUID newInteractiveId = UUID.randomUUID();
        UUID newRootElementId = UUID.randomUUID();

        when(annotationGateway.findCoursewareAnnotation(annotationId)).thenReturn(Mono.just(annotation));
        when(annotationGateway.persist(any(CoursewareAnnotation.class))).thenReturn(Flux.empty());
        DuplicationContext context = new DuplicationContext();
        context.setDuplicatorAccount(accountId);

        CoursewareAnnotation result = annotationDuplicationService.duplicate(newRootElementId, newInteractiveId, context, annotationId).block();

        assertNotNull(result);
        assertTrue(result.getTargetJson().toString().contains(newInteractiveId.toString()));
        assertNotNull(result.getId());
        assertNotEquals(annotation.getId(), result.getId());
        assertNotEquals(annotationId, result.getId());
        assertNotEquals(annotation.getElementId(), result.getElementId());
        assertNotEquals(annotation.getRootElementId(), result.getRootElementId());
    }
}
