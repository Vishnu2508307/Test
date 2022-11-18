package com.smartsparrow.courseware.service;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Flux;

/**
 * Helps in managing delete feature for annotations associated in CoursewareElement Structure
 */
@Singleton
public class AnnotationDeleteService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(AnnotationService.class);

    private final AnnotationService annotationService;
    private final ActivityService activityService;
    private final PathwayService pathwayService;
    private final ComponentService componentService;

    @Inject
    public AnnotationDeleteService(AnnotationService annotationService,
                                   ActivityService activityService,
                                   ComponentService componentService,
                                   PathwayService pathwayService) {
        this.annotationService = annotationService;
        this.activityService = activityService;
        this.pathwayService = pathwayService;
        this.componentService = componentService;
    }

    /**
     * Deletes the annotations of the interactive or activity of Courseware Element Structure recursively
     * @param elementId elementId id which is moved from rootElementId to newRootElementId
     * @param elementType element type in courseware element structure
     * @param rootElementId old root element id
     * @return flux of void
     */
    public Flux<Void> deleteAnnotations(final UUID elementId, final CoursewareElementType elementType, final UUID rootElementId) {
        checkArgument(elementId != null, "missing element id");
        checkArgument(rootElementId != null, "missing root element id");
        checkArgument(elementType != null, "missing element type");

        //delete annotations if any and then call recursively
        switch (elementType) {
            case ACTIVITY:
                return annotationService.deleteAnnotation(rootElementId, elementId).thenMany(
                        activityService.findChildPathwayIds(elementId)
                                .flatMapIterable(pathwayIdList -> pathwayIdList)
                                .flatMap(pathwayId -> deleteAnnotationsForPathwayWalkable(pathwayId, rootElementId)));
            case INTERACTIVE:
                return annotationService.deleteAnnotation(rootElementId, elementId)
                        .thenMany(componentService.findIdsByInteractive(elementId)
                                          .flatMap(componentId -> annotationService.deleteAnnotation(rootElementId, componentId).singleOrEmpty()));
            default:
                return Flux.error(new UnsupportedOperationException(
                        String.format("Broken element type %s. This element type can not have annotations", elementType)));
        }
    }

    /**
     * find pathway walkable(s) and delete annotations for each walkable and its children recursively
     * @param pathwayId pathway id
     * @param rootElementId root element id
     * @return flux of void
     */
    private Flux<Void> deleteAnnotationsForPathwayWalkable(final UUID pathwayId, final UUID rootElementId) {
        return pathwayService.getOrderedWalkableChildren(pathwayId)
                .flatMapIterable(walkableChildList -> walkableChildList)
                .flatMap(walkableChild -> {
                    switch (walkableChild.getElementType()) {
                        case ACTIVITY:
                            //delete annotations if any and then call recursively
                            return annotationService.deleteAnnotation(rootElementId, walkableChild.getElementId())
                                    .thenMany(deleteAnnotations(walkableChild.getElementId(), walkableChild.getElementType(), rootElementId));
                        case INTERACTIVE:
                            return deleteAnnotations(walkableChild.getElementId(), walkableChild.getElementType(), rootElementId);
                        default:
                            return Flux.error(new UnsupportedOperationException(
                                    String.format("Broken pathway %s. Pathway can not have %s as a child", pathwayId, walkableChild)));
                    }
                });
    }
}
