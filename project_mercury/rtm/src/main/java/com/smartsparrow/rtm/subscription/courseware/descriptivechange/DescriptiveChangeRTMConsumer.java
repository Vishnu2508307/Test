package com.smartsparrow.rtm.subscription.courseware.descriptivechange;

import java.util.UUID;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.DescriptiveChangeBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the descriptive change RTM event
 */
public class DescriptiveChangeRTMConsumer implements RTMConsumer<DescriptiveChangeRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new DescriptiveChangeRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the changed activityId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param descriptiveChangeRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, DescriptiveChangeRTMConsumable descriptiveChangeRTMConsumable) {
        final RTMClientContext producingRTMClientContext = descriptiveChangeRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            DescriptiveChangeBroadcastMessage message = descriptiveChangeRTMConsumable.getContent();

            final String broadcastType = descriptiveChangeRTMConsumable.getBroadcastType();
            final UUID subscriptionId = descriptiveChangeRTMConsumable.getSubscriptionId();
            final DescriptiveChangeRTMEventDecoratorImpl rtmEventDecorator = new DescriptiveChangeRTMEventDecoratorImpl(
                    new DescriptiveChangeRTMEvent());

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", message.getElementType())
                    .addField("value", message.getValue())
                    // TODO remove next line when FE supported
                    .addField("action", rtmEventDecorator.getLegacyName())
                    .addField("rtmEvent", rtmEventDecorator.getName(message.getElementType())));
        }
    }

}
