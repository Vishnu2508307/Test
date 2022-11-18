package com.smartsparrow.rtm.message.event.courseware;

import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;
import static com.smartsparrow.dataevent.RouteUri.AUTHOR_ACTIVITY_EVENT;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.courseware.eventmessage.ActivityEventMessage;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.service.AnnotationDeleteService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.rtm.message.event.SimpleEventPublisher;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Mono;

public class DeleteCoursewareAnnotationsEventPublisher extends SimpleEventPublisher<CoursewareElementBroadcastMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(MoveCoursewareAnnotationsEventPublisher.class);

    private final CoursewareService coursewareService;
    private final AnnotationDeleteService annotationDeleteService;

    @Inject
    public DeleteCoursewareAnnotationsEventPublisher(CoursewareService coursewareService,
                                                     AnnotationDeleteService annotationDeleteService) {
        this.coursewareService = coursewareService;
        this.annotationDeleteService = annotationDeleteService;
    }

    /**
     * Use parentPathwayId to find root element id and delete all the annotations associated with root element id and respective element id
     * @param rtmClient holds client context
     * @param broadcastMessage the message to broadcast
     * @throws UnsupportedOperationException when the action argument is not a {@link CoursewareAction#DELETED}
     */
    @Override
    public void publish(RTMClient rtmClient, CoursewareElementBroadcastMessage broadcastMessage) {

        if (!CoursewareAction.DELETED.equals(broadcastMessage.getAction())) {
            throw new UnsupportedOperationException("only DELETED action is allowed for this event publisher");
        }
        //parent element id is pathway broadcast from Handler, find root element id for pathway id
        coursewareService.getRootElementId(broadcastMessage.getParentElement().getElementId(), PATHWAY)
                // with root element id and element id from broadcast delete all the annotations from annotation by motivation only
                .flatMap(rootElementId -> {
                    switch (broadcastMessage.getElement().getElementType()) {
                        case ACTIVITY:
                        case INTERACTIVE:
                            //delete annotations for all element walkable(s) recursively
                            return annotationDeleteService.deleteAnnotations(broadcastMessage.getElement().getElementId(), broadcastMessage.getElement().getElementType(), rootElementId).singleOrEmpty();
                        default:
                            return Mono.error(new UnsupportedOperationException(
                                    String.format("Broken element type %s. This element type can not have annotations delete", broadcastMessage.getElement().getElementType())));
                    }
                })
                .subscribe(parentActivities -> {
                    emitEvent(rtmClient, broadcastMessage, broadcastMessage.getElement().getElementId());
                }, ex -> {
                    log.jsonError("error fetching root element id for for element {}",
                                  new HashMap<String, Object>() {
                                      {
                                          put("elementId", broadcastMessage.getElement().getElementId());
                                          put("error", ex.getStackTrace());
                                      }
                                  },
                                  ex);
                });
    }

    /**
     * Emit an activity event for all the element id
     *
     * @param rtmClient the client that triggered the changes
     * @param broadcastMessage the message containing the event data
     * @param elementId element id
     */

    private void emitEvent(RTMClient rtmClient, CoursewareElementBroadcastMessage broadcastMessage, UUID elementId) {
        ActivityEventMessage eventMessage = new ActivityEventMessage(elementId) //
                .setContent(broadcastMessage) //
                .setProducingClientId(rtmClient.getRtmClientContext().getClientId());

        Mono.just(eventMessage) //
                .map(event -> getCamel().toStream(AUTHOR_ACTIVITY_EVENT, event)) //
                .subscribe();
    }
}
