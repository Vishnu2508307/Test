package com.smartsparrow.rtm.subscription.courseware.moved;

import static com.smartsparrow.courseware.data.CoursewareElementType.COMPONENT;

import java.util.UUID;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the component moved RTM event
 */
public class ComponentMovedRTMConsumer implements RTMConsumer<ComponentMovedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new ComponentMovedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the moved component and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param componentMovedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, ComponentMovedRTMConsumable componentMovedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = componentMovedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            ActivityBroadcastMessage message = componentMovedRTMConsumable.getContent();

            final String broadcastType = componentMovedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = componentMovedRTMConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", COMPONENT)
                    // TODO remove next line when FE supported
                    .addField("action", getRTMEvent().getLegacyName())
                    .addField("rtmEvent", getRTMEvent().getName()));
        }
    }

}
