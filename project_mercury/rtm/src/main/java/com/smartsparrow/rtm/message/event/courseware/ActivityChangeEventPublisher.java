package com.smartsparrow.rtm.message.event.courseware;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.rtm.message.event.SimpleEventPublisher;
import com.smartsparrow.rtm.message.event.lang.EventPublisherException;
import com.smartsparrow.rtm.ws.RTMClient;

import reactor.core.Exceptions;

public class ActivityChangeEventPublisher extends SimpleEventPublisher<CoursewareElementBroadcastMessage> {

    private static final Logger log = LoggerFactory.getLogger(ActivityChangeEventPublisher.class);

    private final CoursewareService coursewareService;

    @Inject
    public ActivityChangeEventPublisher(CoursewareService coursewareService) {
        this.coursewareService = coursewareService;
    }

    @Override
    public void publish(RTMClient rtmClient, CoursewareElementBroadcastMessage data) {

        CoursewareElement element = getCoursewareElement(data);

        if (log.isDebugEnabled()) {
            log.debug("ready to save the changes for element {}", element.toString());
        }

        coursewareService.getParentActivityIds(element.getElementId(), element.getElementType())
                .map(activities -> {
                    if (!activities.isEmpty()) {
                        return activities.get(0);
                    }
                    throw new EventPublisherException("activities cannot be empty");
                })
                .map(coursewareService::saveChange)
                .flatMap(one -> one)
                .subscribe(foo -> {
                    if (log.isDebugEnabled()) {
                        log.debug("saving activity change {}", foo);
                    }
                }, ex -> {
                    if (log.isDebugEnabled()) {
                        log.debug("error saving activity change ", ex);
                    }
                    throw Exceptions.propagate(ex);
                });
    }

    /**
     * Get the courseware element to find the top level activity for. If the action is {@link CoursewareAction#DELETED}
     * the deleted element will not be linked to its parents anymore therefore the parentElement should be returned.
     * For any other action the actual element should be returned.
     *
     * @param data the broadcast message to extract the courseware element from
     * @return a courseware element linked to its parent
     */
    private CoursewareElement getCoursewareElement(CoursewareElementBroadcastMessage data) {
        CoursewareAction action = data.getAction();
        return action.equals(CoursewareAction.DELETED) ? data.getParentElement() : data.getElement();
    }
}
