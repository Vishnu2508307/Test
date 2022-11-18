package com.smartsparrow.rtm.subscription.courseware.configchange;

import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;

import java.util.UUID;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.ConfigChangeBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the pathway configChange RTM event
 */
public class PathwayConfigChangeRTMConsumer implements RTMConsumer<PathwayConfigChangeRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new PathwayConfigChangeRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the changed pathwayId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param pathwayConfigChangeRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, PathwayConfigChangeRTMConsumable pathwayConfigChangeRTMConsumable) {
        final RTMClientContext producingRTMClientContext = pathwayConfigChangeRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            ConfigChangeBroadcastMessage message = pathwayConfigChangeRTMConsumable.getContent();

            final String broadcastType = pathwayConfigChangeRTMConsumable.getBroadcastType();
            final UUID subscriptionId = pathwayConfigChangeRTMConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", PATHWAY)
                    .addField("config", message.getConfig())
                    // TODO remove next line when FE supported
                    .addField("action", getRTMEvent().getLegacyName())
                    .addField("rtmEvent", getRTMEvent().getName()));
        }
    }

}
