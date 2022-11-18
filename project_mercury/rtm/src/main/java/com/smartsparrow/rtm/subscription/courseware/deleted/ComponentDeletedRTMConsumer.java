package com.smartsparrow.rtm.subscription.courseware.deleted;

import static com.smartsparrow.courseware.data.CoursewareElementType.COMPONENT;

import java.util.UUID;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the activity component deleted RTM event
 */
public class ComponentDeletedRTMConsumer implements RTMConsumer<ComponentDeletedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new ComponentDeletedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the deleted activityId, componentId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param componentDeletedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, ComponentDeletedRTMConsumable componentDeletedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = componentDeletedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            ActivityBroadcastMessage message = componentDeletedRTMConsumable.getContent();

            final String broadcastType = componentDeletedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = componentDeletedRTMConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", COMPONENT)
                    // TODO remove next line when FE supported
                    .addField("action", getRTMEvent().getLegacyName())
                    .addField("rtmEvent", getRTMEvent().getName()));
        }
    }

}
