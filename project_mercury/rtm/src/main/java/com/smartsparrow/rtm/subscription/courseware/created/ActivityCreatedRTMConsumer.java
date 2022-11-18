package com.smartsparrow.rtm.subscription.courseware.created;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;

import java.util.UUID;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityCreatedBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the activity created RTM event
 */
public class ActivityCreatedRTMConsumer implements RTMConsumer<ActivityCreatedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new ActivityCreatedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the created activityId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param activityCreatedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, ActivityCreatedRTMConsumable activityCreatedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = activityCreatedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            ActivityCreatedBroadcastMessage message = activityCreatedRTMConsumable.getContent();

            final String broadcastType = activityCreatedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = activityCreatedRTMConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", ACTIVITY)
                    .addField("parentPathwayId", message.getParentPathwayId())
                    // TODO remove next line when FE supported
                    .addField("action", getRTMEvent().getLegacyName())
                    .addField("rtmEvent", getRTMEvent().getName()));
        }
    }

}
