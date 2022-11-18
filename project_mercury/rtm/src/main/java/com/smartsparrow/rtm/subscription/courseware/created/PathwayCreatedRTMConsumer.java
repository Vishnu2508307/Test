package com.smartsparrow.rtm.subscription.courseware.created;

import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;

import java.util.UUID;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.PathwayCreatedBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the activity pathway created RTM event
 */
public class PathwayCreatedRTMConsumer implements RTMConsumer<PathwayCreatedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new PathwayCreatedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the created pathwayId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param pathwayCreatedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, PathwayCreatedRTMConsumable pathwayCreatedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = pathwayCreatedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            PathwayCreatedBroadcastMessage message = pathwayCreatedRTMConsumable.getContent();

            final String broadcastType = pathwayCreatedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = pathwayCreatedRTMConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", PATHWAY)
                    // TODO remove next line when FE supported
                    .addField("action", getRTMEvent().getLegacyName())
                    .addField("rtmEvent", getRTMEvent().getName()));
        }
    }

}
