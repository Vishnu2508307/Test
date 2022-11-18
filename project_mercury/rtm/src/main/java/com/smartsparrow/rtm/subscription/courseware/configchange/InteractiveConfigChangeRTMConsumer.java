package com.smartsparrow.rtm.subscription.courseware.configchange;

import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;

import java.util.UUID;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.ConfigChangeBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the interactive configChange RTM event
 */
public class InteractiveConfigChangeRTMConsumer implements RTMConsumer<InteractiveConfigChangeRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new InteractiveConfigChangeRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the changed interactiveId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param interactiveConfigChangeRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, InteractiveConfigChangeRTMConsumable interactiveConfigChangeRTMConsumable) {
        final RTMClientContext producingRTMClientContext = interactiveConfigChangeRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            ConfigChangeBroadcastMessage message = interactiveConfigChangeRTMConsumable.getContent();

            final String broadcastType = interactiveConfigChangeRTMConsumable.getBroadcastType();
            final UUID subscriptionId = interactiveConfigChangeRTMConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", INTERACTIVE)
                    .addField("config", message.getConfig())
                    // TODO remove next line when FE supported
                    .addField("action", getRTMEvent().getLegacyName())
                    .addField("rtmEvent", getRTMEvent().getName()));
        }
    }

}
