package com.smartsparrow.rtm.message.event.courseware;

import static com.smartsparrow.dataevent.RouteUri.CONTENT_SEEDING_EVENT;

import javax.inject.Inject;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.PublishedActivityBroadcastMessage;
import com.smartsparrow.la.event.ContentSeedingMessage;
import com.smartsparrow.rtm.message.event.SimpleEventPublisher;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

public class ContentSeedingEventPublisher extends SimpleEventPublisher<PublishedActivityBroadcastMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ContentSeedingEventPublisher.class);


    @Inject
    public ContentSeedingEventPublisher() {
    }

    @Override
    public void publish(RTMClient rtmClient, PublishedActivityBroadcastMessage data) {
        log.info("Publishing event to {}", CONTENT_SEEDING_EVENT);
        Mono.just(data)
                .doOnEach(log.reactiveDebugSignal("preparing content seeding message event"))
                .map(event -> {
                    ContentSeedingMessage contentSeedingMessage = new ContentSeedingMessage(data.getCohortId())
                            .setCoursewareElementType(CoursewareElementType.ACTIVITY)
                            .setChangeId(data.getPublishedActivity().getChangeId())
                            .setDeploymentId(data.getPublishedActivity().getId())
                            .setElementId(data.getPublishedActivity().getActivityId());
                    return getCamel().toStream(CONTENT_SEEDING_EVENT, contentSeedingMessage);
                })
                .doOnEach(log.reactiveDebugSignal("Published Content seeding message event"))
                .doOnError(ex -> {
                    ex = Exceptions.unwrap(ex);
                    log.error("could not publish event {} {} {}", CONTENT_SEEDING_EVENT, data, ex.getMessage());
                })
                .subscribe();
    }

}
