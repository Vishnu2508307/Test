package com.smartsparrow.rtm.subscription.courseware.elementthemedelete;

import java.util.UUID;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the element theme delete RTM event
 */
public class ElementThemeDeleteRTMConsumer implements RTMConsumer<ElementThemeDeleteRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new ElementThemeDeleteRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the deleted elementId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param elementThemeDeleteRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, ElementThemeDeleteRTMConsumable elementThemeDeleteRTMConsumable) {
        final RTMClientContext producingRTMClientContext = elementThemeDeleteRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            ActivityBroadcastMessage message = elementThemeDeleteRTMConsumable.getContent();

            final String broadcastType = elementThemeDeleteRTMConsumable.getBroadcastType();
            final UUID subscriptionId = elementThemeDeleteRTMConsumable.getSubscriptionId();
            final ElementThemeDeleteRTMEventDecoratorImpl rtmEventDecorator = new ElementThemeDeleteRTMEventDecoratorImpl(
                    new ElementThemeDeleteRTMEvent());

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", message.getElementType())
                    // TODO remove next line when FE supported
                    .addField("action", rtmEventDecorator.getLegacyName())
                    .addField("rtmEvent", rtmEventDecorator.getName(message.getElementType())));
        }
    }

}
