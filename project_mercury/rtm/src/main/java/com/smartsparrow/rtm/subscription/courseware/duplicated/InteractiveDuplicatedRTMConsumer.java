package com.smartsparrow.rtm.subscription.courseware.duplicated;

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
 * Consumer that handles the activity interactive duplicated RTM event
 */
public class InteractiveDuplicatedRTMConsumer implements RTMConsumer<InteractiveDuplicatedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new InteractiveDuplicatedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the duplicated activityId, interactiveId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param interactiveDuplicatedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, InteractiveDuplicatedRTMConsumable interactiveDuplicatedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = interactiveDuplicatedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            InteractiveCreatedBroadcastMessage message = interactiveDuplicatedRTMConsumable.getContent();

            final String broadcastType = interactiveDuplicatedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = interactiveDuplicatedRTMConsumable.getSubscriptionId();

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
