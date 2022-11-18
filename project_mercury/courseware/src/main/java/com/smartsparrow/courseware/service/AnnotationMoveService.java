package com.smartsparrow.courseware.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.annotation.service.AnnotationService;

import reactor.core.publisher.Flux;

@Singleton
public class AnnotationMoveService {

    private final ActivityService activityService;
    private final PathwayService pathwayService;
    private final AnnotationService annotationService;

    @Inject
    public AnnotationMoveService(ActivityService activityService,
                                 PathwayService pathwayService,
                                 AnnotationService annotationService) {
        this.activityService = activityService;
        this.pathwayService = pathwayService;
        this.annotationService = annotationService;
    }

    /**
     * Find all the annotations for the activity id in all the levels of courseware structure and move to newRootElementId
     * @param activityId activity id which is moved from oldRootElementId to newRootElementId
     * @param oldRootElementId old root element id
     * @param newRootElementId new root element id
     * @return flux of void
     */
    public Flux<Void> moveAnnotations(final UUID activityId, final UUID oldRootElementId, final UUID newRootElementId) {
        checkArgument(activityId != null, "missing activity id");
        checkArgument(oldRootElementId != null, "missing old root element id");
        checkArgument(newRootElementId != null, "missing new root element id");
        return activityService.findChildPathwayIds(activityId)
                .flatMapIterable(pathwayIdList -> pathwayIdList)
                .flatMap(pathwayId -> moveAnnotationsForPathwayWalkable(pathwayId, oldRootElementId, newRootElementId));

    }

    /**
     * find annotations and move to newRootElementId for the provided pathway id
     * @param pathwayId pathway id
     * @param oldRootElementId old root element id
     * @param newRootElementId new root element id
     * @return flux of void
     */
    private Flux<Void> moveAnnotationsForPathwayWalkable(final UUID pathwayId, final UUID oldRootElementId, final UUID newRootElementId) {
        return pathwayService.getOrderedWalkableChildren(pathwayId)
                .flatMapIterable(walkableChildList -> walkableChildList)
                .flatMap(walkableChild -> {
                    switch (walkableChild.getElementType()) {
                        case ACTIVITY:
                            //move annotations if any and then call recursively
                            return annotationService.moveAnnotations(oldRootElementId, walkableChild.getElementId(), newRootElementId)
                                    .thenMany(moveAnnotations(walkableChild.getElementId(), oldRootElementId, newRootElementId));
                        case INTERACTIVE:
                            return annotationService.moveAnnotations(oldRootElementId, walkableChild.getElementId(), newRootElementId);
                        default:
                            return Flux.error(new UnsupportedOperationException(
                                    String.format("Broken pathway %s. Pathway can not have %s as a child", pathwayId, walkableChild)));
                    }
                });
    }
}
