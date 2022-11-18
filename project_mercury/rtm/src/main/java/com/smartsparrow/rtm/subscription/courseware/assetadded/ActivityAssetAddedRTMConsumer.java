package com.smartsparrow.rtm.subscription.courseware.assetadded;

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
 * Consumer that handles the activity asset added RTM event
 */
public class ActivityAssetAddedRTMConsumer implements RTMConsumer<ActivityAssetAddedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new ActivityAssetAddedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the activityId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param activityAssetAddedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, ActivityAssetAddedRTMConsumable activityAssetAddedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = activityAssetAddedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            ActivityCreatedBroadcastMessage message = activityAssetAddedRTMConsumable.getContent();

            final String broadcastType = activityAssetAddedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = activityAssetAddedRTMConsumable.getSubscriptionId();

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
