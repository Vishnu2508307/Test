package com.smartsparrow.rtm.subscription.courseware;

import static com.smartsparrow.rtm.message.handler.courseware.activity.ActivityChangeLogSubscribeMessageHandler.ACTIVITY_CHANGELOG_SUBSCRIBE_BROADCAST;
import static com.smartsparrow.rtm.message.handler.courseware.activity.ActivityChangeLogSubscribeMessageHandler.ACTIVITY_CHANGELOG_SUBSCRIBE_BROADCAST_ERROR;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import org.redisson.api.listener.MessageListener;

import com.smartsparrow.courseware.data.ChangeLogByElement;
import com.smartsparrow.courseware.data.CoursewareChangeLog;
import com.smartsparrow.courseware.eventmessage.ActivityChangeLogEventMessage;
import com.smartsparrow.courseware.eventmessage.CoursewareChangeLogBroadcastMessage;
import com.smartsparrow.courseware.service.CoursewareChangeLogService;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.EventSubscription;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.Exceptions;

public class ActivityChangeLogSubscription extends EventSubscription<ActivityChangeLogEventMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ActivityChangeLogSubscription.class);

    private final CoursewareChangeLogService coursewareChangeLogService;
    private final UUID subscriptionId;

    @Inject
    public ActivityChangeLogSubscription(final CoursewareChangeLogService coursewareChangeLogService) {
        this.coursewareChangeLogService = coursewareChangeLogService;
        this.subscriptionId = UUID.randomUUID();
    }

    public void setActivityId(final UUID activityId) {
        setName(new ActivityChangeLogEventMessage(activityId).getName());
    }

    @Override
    public MessageListener<ActivityChangeLogEventMessage> initMessageListener(RTMClient rtmClient) {
        return (channel, msg) -> {
            // Do nothing if event originates from this same client id
            if (!Objects.equals(msg.getProducingClientId(), rtmClient.getRtmClientContext().getClientId())) {

                CoursewareChangeLogBroadcastMessage content = msg.getContent();
                CoursewareChangeLog changeLog = content.getCoursewareChangeLog();

                // emit an error when an unexpected changeLog type is provided in the message
                if (!(changeLog instanceof ChangeLogByElement)) {
                    log.error("changeLog type not supported by this subscription");
                    emitError(rtmClient, ACTIVITY_CHANGELOG_SUBSCRIBE_BROADCAST_ERROR, msg);
                    return;
                }

                coursewareChangeLogService.getChangeLogForElementPayload((ChangeLogByElement) changeLog)
                        .subscribe(elementChangeLogPayload -> {
                            BasicResponseMessage message = new BasicResponseMessage(ACTIVITY_CHANGELOG_SUBSCRIBE_BROADCAST, subscriptionId.toString())
                                    .addField("changeLog", elementChangeLogPayload);

                            emitSuccess(rtmClient, message);
                        }, ex -> {
                            ex = Exceptions.unwrap(ex);
                            log.error("error fetching the changelog payload", ex);
                            emitError(rtmClient, ACTIVITY_CHANGELOG_SUBSCRIBE_BROADCAST_ERROR, msg);
                        });
            }
        };
    }

    @Override
    public String getId() {
        return subscriptionId.toString();
    }

    @Override
    public Class<ActivityChangeLogEventMessage> getMessageType() {
        return ActivityChangeLogEventMessage.class;
    }
}
