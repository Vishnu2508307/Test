package com.smartsparrow.rtm.subscription.courseware.configchange;

import static com.smartsparrow.courseware.data.CoursewareElementType.COMPONENT;

import java.util.UUID;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.ConfigChangeBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the component configChange RTM event
 */
public class ComponentConfigChangeRTMConsumer implements RTMConsumer<ComponentConfigChangeRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new ComponentConfigChangeRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the changed componentId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param componentConfigChangeRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, ComponentConfigChangeRTMConsumable componentConfigChangeRTMConsumable) {
        final RTMClientContext producingRTMClientContext = componentConfigChangeRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            ConfigChangeBroadcastMessage message = componentConfigChangeRTMConsumable.getContent();

            final String broadcastType = componentConfigChangeRTMConsumable.getBroadcastType();
            final UUID subscriptionId = componentConfigChangeRTMConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", COMPONENT)
                    .addField("config", message.getConfig())
                    // TODO remove next line when FE supported
                    .addField("action", getRTMEvent().getLegacyName())
                    .addField("rtmEvent", getRTMEvent().getName()));
        }
    }

}
