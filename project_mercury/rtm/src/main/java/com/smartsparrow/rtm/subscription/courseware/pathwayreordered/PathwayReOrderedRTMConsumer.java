package com.smartsparrow.rtm.subscription.courseware.pathwayreordered;

import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;

import java.util.UUID;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.PathwayReOrderedBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the pathway reordered RTM event
 */
public class PathwayReOrderedRTMConsumer implements RTMConsumer<PathwayReOrderedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new PathwayReOrderedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the pathway reordered id and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param pathwayReOrderedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, PathwayReOrderedRTMConsumable pathwayReOrderedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = pathwayReOrderedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            PathwayReOrderedBroadcastMessage message = pathwayReOrderedRTMConsumable.getContent();

            final String broadcastType = pathwayReOrderedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = pathwayReOrderedRTMConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", PATHWAY)
                    .addField("walkables", message.getWalkables())
                    // TODO remove next line when FE supported
                    .addField("action", getRTMEvent().getLegacyName())
                    .addField("rtmEvent", getRTMEvent().getName()));
        }
    }

}
