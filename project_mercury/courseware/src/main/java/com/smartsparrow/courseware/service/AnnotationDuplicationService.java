package com.smartsparrow.courseware.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.json.JSONArray;
import org.json.JSONObject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.annotation.data.AnnotationGateway;
import com.smartsparrow.annotation.service.CoursewareAnnotation;
import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.util.Json;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class AnnotationDuplicationService {

    private final AnnotationGateway annotationGateway;

    @Inject
    public AnnotationDuplicationService(AnnotationGateway annotationGateway) {
        this.annotationGateway = annotationGateway;
    }

    /**
     * Finds a list of Annotation IDs for element and root element
     *
     * @param rootElementId the root element id
     * @param elementId     the element id
     * @return Flux of UUIDs, can be empty
     */
    @Trace(async = true)
    public Flux<UUID> findIdsByElement(final UUID rootElementId, final UUID elementId) {
        checkArgument(rootElementId != null, "missing root element id");
        checkArgument(elementId != null, "missing element id");

        return Flux.merge(
                annotationGateway.findCoursewareAnnotation(rootElementId, elementId, Motivation.classifying),
                annotationGateway.findCoursewareAnnotation(rootElementId, elementId, Motivation.linking),
                annotationGateway.findCoursewareAnnotation(rootElementId, elementId, Motivation.identifying),
                annotationGateway.findCoursewareAnnotation(rootElementId, elementId, Motivation.tagging)
        ).map(CoursewareAnnotation::getId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Duplicate annotation and attach annotation to the given element.
     *
     * @param rootElementId    the rootElementId
     * @param newElementId     the new element id
     * @param context          keeps pairs old ids/new ids
     * @param annotationId     the id of the annotation to copy
     * @return mono with created annotation copy
     */
    @Trace(async = true)
    public Mono<CoursewareAnnotation> duplicate(final UUID rootElementId, final UUID newElementId, final DuplicationContext context, final UUID annotationId) {

        return annotationGateway.findCoursewareAnnotation(annotationId)
                //copy annotation object itself and attach to element
                .flatMap(annotation -> duplicateAnnotation(annotation, rootElementId, newElementId, context.getDuplicatorAccount(), annotationId)
                        .doOnSuccess(newAnnotation -> context.putIds(annotationId, newAnnotation.getId())))
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

    /**
     * Duplicate annotation and attach annotation to the given element.
     *
     * @param annotation       the annotation to copy
     * @param rootElementId    the rootElementId
     * @param newElementId     the new element id
     * @return mono with created annotation copy
     */
    @Trace(async = true)
    Mono<CoursewareAnnotation> duplicateAnnotation(final CoursewareAnnotation annotation, final UUID rootElementId, final UUID newElementId, final UUID accountId, final UUID oldAnnotationId) {
        UUID id = UUIDs.timeBased();

        CoursewareAnnotation newAnnotation = new CoursewareAnnotation()
                .setId(id)
                .setVersion(id)
                .setMotivation(annotation.getMotivation())
                .setRootElementId(rootElementId)
                .setElementId(newElementId)
                .setBodyJson(annotation.getBodyJson())
                .setCreatorAccountId(accountId);

        //updating new INTERACTIVE id to the target JSON
        if(annotation.getTargetJson().has(0)) {
            JSONArray targetJson = new JSONArray(annotation.getTargetJson().toString());
            if (((JSONObject) targetJson.get(0)).has("id")) {
                ((JSONObject) targetJson.get(0)).put("id", newElementId);
                newAnnotation.setTargetJson(Json.toJsonNode(targetJson.toString()));
            }
        } else {
            newAnnotation.setTargetJson(annotation.getTargetJson());
        }

        return annotationGateway.persist(newAnnotation)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .then(Mono.just(newAnnotation));
    }
}
