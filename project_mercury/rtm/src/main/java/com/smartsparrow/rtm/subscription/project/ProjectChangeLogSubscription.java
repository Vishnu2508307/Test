package com.smartsparrow.rtm.subscription.project;

import static com.smartsparrow.rtm.message.handler.workspace.ProjectChangeLogSubscribeMessageHandler.PROJECT_CHANGELOG_SUBSCRIBE_BROADCAST;
import static com.smartsparrow.rtm.message.handler.workspace.ProjectChangeLogSubscribeMessageHandler.PROJECT_CHANGELOG_SUBSCRIBE_BROADCAST_ERROR;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import org.redisson.api.listener.MessageListener;

import com.smartsparrow.courseware.data.ChangeLogByProject;
import com.smartsparrow.courseware.data.CoursewareChangeLog;
import com.smartsparrow.courseware.eventmessage.CoursewareChangeLogBroadcastMessage;
import com.smartsparrow.courseware.eventmessage.ProjectChangeLogEventMessage;
import com.smartsparrow.courseware.service.CoursewareChangeLogService;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.EventSubscription;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.Exceptions;

public class ProjectChangeLogSubscription extends EventSubscription<ProjectChangeLogEventMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ProjectChangeLogSubscription.class);

    private final CoursewareChangeLogService coursewareChangeLogService;
    private final UUID subscriptionId;

    @Inject
    public ProjectChangeLogSubscription(final CoursewareChangeLogService coursewareChangeLogService) {
        this.coursewareChangeLogService = coursewareChangeLogService;
        this.subscriptionId = UUID.randomUUID();
    }

    public void setProjectId(final UUID projectId) {
        setName(new ProjectChangeLogEventMessage(projectId).getName());
    }

    @Override
    public MessageListener<ProjectChangeLogEventMessage> initMessageListener(RTMClient rtmClient) {
        return (channel, msg) -> {
            // Do nothing if event originates from this same client id
            if (!Objects.equals(msg.getProducingClientId(), rtmClient.getRtmClientContext().getClientId())) {

                CoursewareChangeLogBroadcastMessage content = msg.getContent();
                CoursewareChangeLog changeLog = content.getCoursewareChangeLog();

                // emit an error when an unexpected changeLog type is provided in the message
                if (!(changeLog instanceof ChangeLogByProject)) {
                    log.error("changeLog type not supported by this subscription");
                    emitError(rtmClient, PROJECT_CHANGELOG_SUBSCRIBE_BROADCAST_ERROR, msg);
                    return;
                }

                coursewareChangeLogService.getChangeLogForProjectPayload((ChangeLogByProject) changeLog)
                        .subscribe(projectChangeLogPayload -> {
                            BasicResponseMessage message = new BasicResponseMessage(PROJECT_CHANGELOG_SUBSCRIBE_BROADCAST, subscriptionId.toString())
                                    .addField("changeLog", projectChangeLogPayload);

                            emitSuccess(rtmClient, message);
                        }, ex -> {
                            ex = Exceptions.unwrap(ex);
                            log.jsonError("error fetching the changelog payload", new HashMap<String, Object>() {
                                {put("clientId", rtmClient.getRtmClientContext().getClientId());}
                            }, ex);
                            emitError(rtmClient, PROJECT_CHANGELOG_SUBSCRIBE_BROADCAST_ERROR, msg);
                        });
            }
        };
    }

    @Override
    public String getId() {
        return subscriptionId.toString();
    }

    @Override
    public Class<ProjectChangeLogEventMessage> getMessageType() {
        return ProjectChangeLogEventMessage.class;
    }
}
