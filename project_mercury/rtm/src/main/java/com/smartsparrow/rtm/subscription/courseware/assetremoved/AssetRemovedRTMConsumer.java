package com.smartsparrow.rtm.subscription.courseware.assetremoved;

import java.util.UUID;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.AssetRemovedBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the asset removed RTM event
 */
public class AssetRemovedRTMConsumer implements RTMConsumer<AssetRemovedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new AssetRemovedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the elementId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param assetRemovedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, AssetRemovedRTMConsumable assetRemovedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = assetRemovedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            AssetRemovedBroadcastMessage message = assetRemovedRTMConsumable.getContent();

            final String broadcastType = assetRemovedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = assetRemovedRTMConsumable.getSubscriptionId();
            final AssetRemovedRTMEventDecoratorImpl rtmEventDecorator = new AssetRemovedRTMEventDecoratorImpl(
                    new AssetRemovedRTMEvent());

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", message.getElementType())
                    // TODO remove next line when FE supported
                    .addField("action", rtmEventDecorator.getLegacyName())
                    .addField("rtmEvent", rtmEventDecorator.getName(message.getElementType())));
        }
    }

}
