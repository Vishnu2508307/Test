package com.smartsparrow.rtm.message.event.courseware;

import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;
import static com.smartsparrow.dataevent.RouteUri.AUTHOR_ACTIVITY_EVENT;

import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.courseware.eventmessage.ActivityEventMessage;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.courseware.service.AnnotationMoveService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.rtm.message.event.SimpleEventPublisher;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Mono;

public class MoveCoursewareAnnotationsEventPublisher extends SimpleEventPublisher<CoursewareElementBroadcastMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(MoveCoursewareAnnotationsEventPublisher.class);

    private final CoursewareService coursewareService;
    private final AnnotationMoveService annotationMoveService;

    @Inject
    public MoveCoursewareAnnotationsEventPublisher(final CoursewareService coursewareService,
                                                   final AnnotationMoveService annotationMoveService) {
        this.coursewareService = coursewareService;
        this.annotationMoveService = annotationMoveService;
    }

    @Override
    public void publish(RTMClient rtmClient, CoursewareElementBroadcastMessage broadcastMessage) {
        //find root element id based on old/new Pathway ID which has been broadcast from Handler
        coursewareService.getRootElementId(broadcastMessage.getOldParentElement().getElementId(), PATHWAY)
                .flatMap(oldRootElementId -> coursewareService.getRootElementId(broadcastMessage.getParentElement().getElementId(), PATHWAY)
                        //find pathway id for moved activity id and move annotations in all the levels of courseware structure
                        .flatMap(newRootElementId -> annotationMoveService.moveAnnotations(broadcastMessage.getElement().getElementId(), oldRootElementId, newRootElementId).singleOrEmpty()))
                .subscribe(parentActivities -> {
                    emitEvent(rtmClient, broadcastMessage, broadcastMessage.getElement().getElementId());
                }, ex -> {
                    log.jsonError("error fetching parent activities for element {}",
                                  new HashMap<String, Object>() {
                                      {
                                          put("elementId", broadcastMessage.getElement().getElementId());
                                          put("error", ex.getStackTrace());
                                      }
                                  },
                                  ex);
                });;
    }



    /**
     * Emit an activity event for all the parent activity
     *
     * @param rtmClient the client that triggered the changes
     * @param broadcastMessage the message containing the event data
     * @param parentActivity the list of parent activities present in the tree
     */
    private void emitEvent(RTMClient rtmClient, CoursewareElementBroadcastMessage broadcastMessage, UUID parentActivity) {
        ActivityEventMessage eventMessage = new ActivityEventMessage(parentActivity) //
                .setContent(broadcastMessage) //
                .setProducingClientId(rtmClient.getRtmClientContext().getClientId());

        Mono.just(eventMessage) //
                .map(event -> getCamel().toStream(AUTHOR_ACTIVITY_EVENT, event)) //
                .subscribe();
    }
}
