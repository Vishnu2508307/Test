package com.smartsparrow.courseware.route;

import static com.smartsparrow.dataevent.RouteUri.ACTIVITY_CHANGELOG_EVENT;
import static com.smartsparrow.dataevent.RouteUri.AUTHOR_ACTIVITY_EVENT;
import static com.smartsparrow.dataevent.RouteUri.DIRECT;
import static com.smartsparrow.dataevent.RouteUri.FIREHOSE;
import static com.smartsparrow.dataevent.RouteUri.PROJECT_CHANGELOG_EVENT;
import static com.smartsparrow.dataevent.RouteUri.RS;

import javax.inject.Inject;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

import com.smartsparrow.cache.service.CacheService;

public class CoursewareRoutes extends RouteBuilder {

    // Header/Property constants
    public static final String EVALUATION_EVENT_MESSAGE = "evaluationEventMessage";
    public static final String ACTION_PROGRESS_CONTEXT = "actionProgressContext";

    @Inject
    private CacheService cacheService;


    @Override
    public void configure() {

        // TODO remove after finish wiring MoveCoursewareAnnotationsEventPublisher and DeleteCoursewareAnnotationsEventPublisher
        // Activity routes
        from(RS + AUTHOR_ACTIVITY_EVENT)
                .id("ActivityEvent")
                .log(LoggingLevel.DEBUG, "Received activity event ${in.body}")
                .wireTap(DIRECT + FIREHOSE)
                .bean(cacheService);

        // Project changelog event
        from(RS + PROJECT_CHANGELOG_EVENT)
                .id("ProjectChangeLogEvent")
                .log(LoggingLevel.DEBUG, "Received project changelog event ${in.body}")
                .wireTap(DIRECT + FIREHOSE)
                .bean(cacheService);

        // activity changelog event
        from(RS + ACTIVITY_CHANGELOG_EVENT)
                .id("ActivityChangeLogEvent")
                .log(LoggingLevel.DEBUG, "Received activity changelog event ${in.body}")
                .wireTap(DIRECT + FIREHOSE)
                .bean(cacheService);

    }
}
