package com.smartsparrow.rtm.subscription.workspace;

import java.util.UUID;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the project created RTM event
 */
public class ProjectCreatedRTMConsumer implements RTMConsumer<ProjectCreatedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new ProjectCreatedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the workspaceId the created projectId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param projectCreatedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, ProjectCreatedRTMConsumable projectCreatedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = projectCreatedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            ProjectCreatedBroadcastMessage message = projectCreatedRTMConsumable.getContent();

            final String broadcastType = projectCreatedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = projectCreatedRTMConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("workspaceId", message.getWorkspaceId())
                    .addField("projectId", message.getProjectId())
                    // TODO remove next line when FE supported
                    .addField("action", getRTMEvent().getLegacyName())
                    .addField("rtmEvent", getRTMEvent().getName()));
        }
    }

}
