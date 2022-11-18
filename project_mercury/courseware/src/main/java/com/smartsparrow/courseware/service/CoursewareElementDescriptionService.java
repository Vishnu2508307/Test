package com.smartsparrow.courseware.service;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.CoursewareElementDescriptionGateway;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class CoursewareElementDescriptionService {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CoursewareElementDescriptionService.class);

    private final CoursewareElementDescriptionGateway coursewareElementDescriptionGateway;

    @Inject
    public CoursewareElementDescriptionService(final CoursewareElementDescriptionGateway coursewareElementDescriptionGateway) {
        this.coursewareElementDescriptionGateway = coursewareElementDescriptionGateway;
    }

    /**
     * Persist a courseware element description for an element
     *
     * @param elementId   the element to track the changelog by
     * @param elementType the element type
     * @param description the element description for a courseware element
     * @return a mono with the created element description
     */
    public Mono<CoursewareElementDescription> createCoursewareElementDescription(final UUID elementId,
                                                                                 final CoursewareElementType elementType,
                                                                                 final String description) {
        affirmArgument(elementId != null, "elementId is required");
        affirmArgument(elementType != null, "elementType is required");
        affirmArgument(description != null, "description is required");

        CoursewareElementDescription coursewareElementDescription = new CoursewareElementDescription()
                .setElementId(elementId)
                .setElementType(elementType)
                .setValue(description);

        return coursewareElementDescriptionGateway.persist(coursewareElementDescription)
                .then(Mono.just(coursewareElementDescription));
    }

    /**
     * Fetch courseware element description by courseware element id.
     *
     * @param elementId the courseware element id.
     * @return {@link Flux < CoursewareElementDescription >} Flux of element description for element
     */
    @Trace(async = true)
    public Mono<CoursewareElementDescription> fetchCoursewareDescriptionByElement(final UUID elementId) {
        affirmArgument(elementId != null, "elementId is required");
        return coursewareElementDescriptionGateway.fetchDescriptionByElement(elementId)
                .doOnEach(ReactiveTransaction.linkOnNext());
    }

}
