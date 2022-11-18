package com.smartsparrow.rtm.subscription.courseware.assetsremoved;

import java.util.UUID;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the assets removed RTM event
 */
public class AssetsRemovedRTMConsumer implements RTMConsumer<AssetsRemovedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new AssetsRemovedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the elementId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param assetsRemovedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, AssetsRemovedRTMConsumable assetsRemovedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = assetsRemovedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            ActivityBroadcastMessage message = assetsRemovedRTMConsumable.getContent();

            final String broadcastType = assetsRemovedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = assetsRemovedRTMConsumable.getSubscriptionId();
            final AssetsRemovedRTMEventDecoratorImpl rtmEventDecorator = new AssetsRemovedRTMEventDecoratorImpl(
                    new AssetsRemovedRTMEvent());

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", message.getElementType())
                    // TODO remove next line when FE supported
                    .addField("action", rtmEventDecorator.getLegacyName())
                    .addField("rtmEvent", rtmEventDecorator.getName(message.getElementType())));
        }
    }

}
