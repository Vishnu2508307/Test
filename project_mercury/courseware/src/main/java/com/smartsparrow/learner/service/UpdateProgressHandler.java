package com.smartsparrow.learner.service;


import java.util.List;

import javax.inject.Singleton;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.learner.event.UpdateCoursewareElementProgressEvent;
import com.smartsparrow.learner.event.UpdateProgressMessage;
import com.smartsparrow.learner.progress.Progress;
import com.smartsparrow.pubsub.subscriptions.studentprogress.StudentProgressRTMProducer;

/**
 * Base class to provide helper functions for common operations across a progress update operation
 */
@Singleton
public abstract class UpdateProgressHandler {

    private static final Logger log = LoggerFactory.getLogger(UpdateProgressHandler.class);

    private final StudentProgressRTMProducer studentProgressRTMProducer;

    protected UpdateProgressHandler(StudentProgressRTMProducer studentProgressRTMProducer) {
        this.studentProgressRTMProducer = studentProgressRTMProducer;
    }

    CoursewareElement findAncestor(final UpdateCoursewareElementProgressEvent event) {
        //
        CoursewareElement me = event.getElement();
        List<CoursewareElement> eventAncestry = event.getUpdateProgressEvent().getAncestryList();

        int i = eventAncestry.indexOf(me);
        // check if we are the root or would be out of bounds.
        if (i == -1 || i + 1 >= eventAncestry.size()) {
            return null;
        }

        // return one plus me.
        return eventAncestry.get(i + 1);
    }

    CoursewareElement findDescendent(final UpdateCoursewareElementProgressEvent event) {
        //
        CoursewareElement me = event.getElement();
        List<CoursewareElement> eventAncestry = event.getUpdateProgressEvent().getAncestryList();

        int i = eventAncestry.indexOf(me);
        // check if we would be out of bounds.
        if (i - 1 < 0) {
            return null;
        }

        // return one plus me.
        return eventAncestry.get(i - 1);
    }

    /**
     * Broadcast the progress to subscribers.
     *
     * @param newProgress the newly created progress to broadcast to subscribers
     * @param evaluationEvent the evaluation event needed to get student id and client id which triggered the event
     */
    void broadcastProgressEventMessage(final Progress newProgress,
            final UpdateProgressMessage evaluationEvent) {
        log.info("broadcast progress: {}", newProgress);

        // Create event notifying listeners about progress upgrade
        studentProgressRTMProducer
                .buildStudentProgressRTMConsumable(evaluationEvent.getStudentId(),
                                                   newProgress.getCoursewareElementId(),
                                                   newProgress.getDeploymentId(),
                                                   newProgress).produce();
    }

    void propagateProgressChangeUpwards(final Exchange exchange,
                                        final UpdateCoursewareElementProgressEvent event,
                                        final Progress progress) {

        CoursewareElement to = findAncestor(event);

        // If no more ancestors (we're at root level) mark the finished header and return the most recent progress
        // UpdateCoursewareElementProgressEvent, as it contains all the progress calculated in this route
        if(to == null) {

            // mark the route as done to break recursion in content router
            exchange.getIn().setHeader("progressDone", true);

            // set the latest event in the OUT body, so it gets returned to callee route
            event.getEventProgress().add(progress);
            exchange.getOut().setBody(event);
            return;
        }

        // Prep to recurse the progress router
        // rebuild the event.
        UpdateCoursewareElementProgressEvent u = new UpdateCoursewareElementProgressEvent() //
                .setUpdateProgressEvent(event.getUpdateProgressEvent()) //
                .setElement(to);

        // copy in the progress (from interactive upwards)
        if (event.getUpdateProgressEvent() != null) {
            u.getEventProgress().addAll(event.getEventProgress());
        }
        u.getEventProgress().add(progress);

        exchange.getIn().setHeader("progressDone", false);
        exchange.getIn().setBody(u);

    }
}
