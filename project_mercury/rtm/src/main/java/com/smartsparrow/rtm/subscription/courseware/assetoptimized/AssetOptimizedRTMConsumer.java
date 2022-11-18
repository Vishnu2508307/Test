package com.smartsparrow.rtm.subscription.courseware.assetoptimized;

import java.util.UUID;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.AssetOptimizedBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the asset optimized RTM event
 */
public class AssetOptimizedRTMConsumer implements RTMConsumer<AssetOptimizedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new AssetOptimizedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the elementId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param activityAssetOptimizedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, AssetOptimizedRTMConsumable activityAssetOptimizedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = activityAssetOptimizedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            AssetOptimizedBroadcastMessage message = activityAssetOptimizedRTMConsumable.getContent();

            final String broadcastType = activityAssetOptimizedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = activityAssetOptimizedRTMConsumable.getSubscriptionId();
            final AssetOptimizedRTMEventDecoratorImpl rtmEventDecorator = new AssetOptimizedRTMEventDecoratorImpl(
                    new AssetOptimizedRTMEvent());

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", message.getElementType())
                    .addField("assetId", message.getAssetId())
                    .addField("assetUrl", message.getAssetUrl())
                    // TODO remove next line when FE supported
                    .addField("action", rtmEventDecorator.getLegacyName())
                    .addField("rtmEvent", rtmEventDecorator.getName(message.getElementType())));
        }
    }

}
