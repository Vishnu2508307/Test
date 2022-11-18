package com.smartsparrow.rtm.subscription.courseware.created;

import static com.smartsparrow.courseware.data.CoursewareElementType.COMPONENT;

import java.util.UUID;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.ComponentCreatedBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the activity component created RTM event
 */
public class ComponentCreatedRTMConsumer implements RTMConsumer<ComponentCreatedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new ComponentCreatedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the created activityId, componentId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param componentCreatedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, ComponentCreatedRTMConsumable componentCreatedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = componentCreatedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            ComponentCreatedBroadcastMessage message = componentCreatedRTMConsumable.getContent();

            final String broadcastType = componentCreatedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = componentCreatedRTMConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", COMPONENT)
                    // TODO remove next line when FE supported
                    .addField("action", getRTMEvent().getLegacyName())
                    .addField("rtmEvent", getRTMEvent().getName()));
        }
    }

}
