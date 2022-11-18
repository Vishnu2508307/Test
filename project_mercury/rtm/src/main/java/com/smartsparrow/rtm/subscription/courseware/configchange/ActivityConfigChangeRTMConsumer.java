package com.smartsparrow.rtm.subscription.courseware.configchange;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;

import java.util.UUID;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.ConfigChangeBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the activity configChange RTM event
 */
public class ActivityConfigChangeRTMConsumer implements RTMConsumer<ActivityConfigChangeRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new ActivityConfigChangeRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the changed activityId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param activityConfigChangeRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, ActivityConfigChangeRTMConsumable activityConfigChangeRTMConsumable) {
        final RTMClientContext producingRTMClientContext = activityConfigChangeRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            ConfigChangeBroadcastMessage message = activityConfigChangeRTMConsumable.getContent();

            final String broadcastType = activityConfigChangeRTMConsumable.getBroadcastType();
            final UUID subscriptionId = activityConfigChangeRTMConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", ACTIVITY)
                    .addField("config", message.getConfig())
                    // TODO remove next line when FE supported
                    .addField("action", getRTMEvent().getLegacyName())
                    .addField("rtmEvent", getRTMEvent().getName()));
        }
    }

}
