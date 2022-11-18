package com.smartsparrow.rtm.message.event.workspace;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.smartsparrow.rtm.message.event.SimpleEventPublisher;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.workspace.subscription.ProjectBroadcastMessage;
import com.smartsparrow.workspace.subscription.ProjectEventProducer;

public class ProjectEventPublisher extends SimpleEventPublisher<ProjectBroadcastMessage> {

    private static final Logger log = LoggerFactory.getLogger(ProjectEventPublisher.class);

    private final ProjectEventProducer projectEventProducer;

    @Inject
    public ProjectEventPublisher(final ProjectEventProducer projectEventProducer) {
        this.projectEventProducer = projectEventProducer;
    }

    @Override
    public void publish(RTMClient rtmClient, ProjectBroadcastMessage broadcastMessage) {
        projectEventProducer.buildProjectEventConsumable(broadcastMessage.getProjectId(),
                                                         broadcastMessage.getIngestionId(),
                                                         broadcastMessage.getIngestionStatus())
                .produce();
    }

}
