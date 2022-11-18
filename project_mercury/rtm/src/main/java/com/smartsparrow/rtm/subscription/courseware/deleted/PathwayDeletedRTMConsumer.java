package com.smartsparrow.rtm.subscription.courseware.deleted;

import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;

import java.util.UUID;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the activity pathway deleted RTM event
 */
public class PathwayDeletedRTMConsumer implements RTMConsumer<PathwayDeletedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new PathwayDeletedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the deleted activityId, pathwayId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param pathwayDeletedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, PathwayDeletedRTMConsumable pathwayDeletedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = pathwayDeletedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            ActivityBroadcastMessage message = pathwayDeletedRTMConsumable.getContent();

            final String broadcastType = pathwayDeletedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = pathwayDeletedRTMConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", PATHWAY)
                    // TODO remove next line when FE supported
                    .addField("action", getRTMEvent().getLegacyName())
                    .addField("rtmEvent", getRTMEvent().getName()));
        }
    }

}
