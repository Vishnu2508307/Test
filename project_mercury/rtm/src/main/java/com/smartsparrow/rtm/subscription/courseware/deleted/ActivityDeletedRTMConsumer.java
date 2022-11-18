package com.smartsparrow.rtm.subscription.courseware.deleted;

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
 * Consumer that handles the activity deleted RTM event
 */
public class ActivityDeletedRTMConsumer implements RTMConsumer<ActivityDeletedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new ActivityDeletedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the deleted activityId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param activityDeletedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, ActivityDeletedRTMConsumable activityDeletedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = activityDeletedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            ActivityCreatedBroadcastMessage message = activityDeletedRTMConsumable.getContent();

            final String broadcastType = activityDeletedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = activityDeletedRTMConsumable.getSubscriptionId();

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
