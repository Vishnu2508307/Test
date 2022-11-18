package com.smartsparrow.rtm.subscription.courseware.elementthemecreate;

import java.util.UUID;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.ThemeBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the element theme create RTM event
 */
public class ElementThemeCreateRTMConsumer implements RTMConsumer<ElementThemeCreateRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new ElementThemeCreateRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the created elementId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param elementThemeCreateRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, ElementThemeCreateRTMConsumable elementThemeCreateRTMConsumable) {
        final RTMClientContext producingRTMClientContext = elementThemeCreateRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            ThemeBroadcastMessage message = elementThemeCreateRTMConsumable.getContent();

            final String broadcastType = elementThemeCreateRTMConsumable.getBroadcastType();
            final UUID subscriptionId = elementThemeCreateRTMConsumable.getSubscriptionId();
            final ElementThemeCreateRTMEventDecoratorImpl rtmEventDecorator = new ElementThemeCreateRTMEventDecoratorImpl(
                    new ElementThemeCreateRTMEvent());

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", message.getElementType())
                    .addField("themeId", message.getThemeId())
                    // TODO remove next line when FE supported
                    .addField("action", rtmEventDecorator.getLegacyName())
                    .addField("rtmEvent", rtmEventDecorator.getName(message.getElementType())));
        }
    }

}
