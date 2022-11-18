package com.smartsparrow.rtm.subscription.courseware.assetadded;

import java.util.UUID;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.AssetAddedBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the asset added RTM event
 */
public class AssetAddedRTMConsumer implements RTMConsumer<AssetAddedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new AssetAddedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the created elementId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param assetAddedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, AssetAddedRTMConsumable assetAddedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = assetAddedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            AssetAddedBroadcastMessage message = assetAddedRTMConsumable.getContent();

            final String broadcastType = assetAddedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = assetAddedRTMConsumable.getSubscriptionId();
            final AssetAddedRTMEventDecoratorImpl rtmEventDecorator = new AssetAddedRTMEventDecoratorImpl(
                    new AssetAddedRTMEvent());

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", message.getElementType())
                    // TODO remove next line when FE supported
                    .addField("action", rtmEventDecorator.getLegacyName())
                    .addField("rtmEvent", rtmEventDecorator.getName(message.getElementType())));
        }
    }

}
