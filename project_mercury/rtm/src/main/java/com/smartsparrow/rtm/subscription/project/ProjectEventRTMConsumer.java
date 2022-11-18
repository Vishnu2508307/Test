package com.smartsparrow.rtm.subscription.project;

import java.util.UUID;

import com.smartsparrow.pubsub.data.RTMEvent;

import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.workspace.subscription.ProjectBroadcastMessage;
import com.smartsparrow.workspace.subscription.ProjectEventConsumable;
import com.smartsparrow.workspace.subscription.ProjectRTMEvent;

public class ProjectEventRTMConsumer implements RTMConsumer<ProjectEventConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new ProjectRTMEvent();
    }

    @Override
    public void accept(final RTMClient rtmClient,
                       final ProjectEventConsumable projectEventConsumable) {

        ProjectBroadcastMessage content = projectEventConsumable.getContent();
        final String broadcastType = projectEventConsumable.getBroadcastType();
        final UUID subscriptionId = projectEventConsumable.getSubscriptionId();

        Responses.writeReactive(rtmClient.getSession(),
                                new BasicResponseMessage(broadcastType, subscriptionId.toString())
                                        .addField("projectId", content.getProjectId())
                                        .addField("ingestionId", content.getIngestionId())
                                        .addField("ingestionStatus", content.getIngestionStatus())
                                        .addField("rtmEvent", getRTMEvent().getName()));
    }
}


