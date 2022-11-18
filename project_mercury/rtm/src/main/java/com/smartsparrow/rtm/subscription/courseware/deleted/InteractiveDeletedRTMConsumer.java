package com.smartsparrow.rtm.subscription.courseware.deleted;

import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;

import java.util.UUID;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.InteractiveCreatedBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the activity interactive deleted RTM event
 */
public class InteractiveDeletedRTMConsumer implements RTMConsumer<InteractiveDeletedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new InteractiveDeletedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the deleted activityId, interactiveId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param interactiveDeletedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, InteractiveDeletedRTMConsumable interactiveDeletedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = interactiveDeletedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            InteractiveCreatedBroadcastMessage message = interactiveDeletedRTMConsumable.getContent();

            final String broadcastType = interactiveDeletedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = interactiveDeletedRTMConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", INTERACTIVE)
                    .addField("parentPathwayId", message.getParentPathwayId())
                    // TODO remove next line when FE supported
                    .addField("action", getRTMEvent().getLegacyName())
                    .addField("rtmEvent", getRTMEvent().getName()));
        }
    }

}
