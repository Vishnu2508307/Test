package com.smartsparrow.rtm.subscription.courseware.moved;

import java.util.UUID;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.ElementMovedBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the activity moved RTM event
 */
public class ActivityMovedRTMConsumer implements RTMConsumer<ActivityMovedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new ActivityMovedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the moved activityId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param activityMovedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, ActivityMovedRTMConsumable activityMovedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = activityMovedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            ElementMovedBroadcastMessage message = activityMovedRTMConsumable.getContent();

            final String broadcastType = activityMovedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = activityMovedRTMConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", message.getElementType())
                    .addField("fromPathwayId", message.getFromPathwayId())
                    .addField("toPathwayId", message.getToPathwayId())
                    // TODO remove next line when FE supported
                    .addField("action", getRTMEvent().getLegacyName())
                    .addField("rtmEvent", getRTMEvent().getName()));
        }
    }

}
