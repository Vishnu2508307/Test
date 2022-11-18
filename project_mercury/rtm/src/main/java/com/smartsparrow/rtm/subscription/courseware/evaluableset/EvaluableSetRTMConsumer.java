package com.smartsparrow.rtm.subscription.courseware.evaluableset;

import java.util.UUID;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.EvaluableSetBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the evaluable set RTM event
 */
public class EvaluableSetRTMConsumer implements RTMConsumer<EvaluableSetRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new EvaluableSetRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the elementId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param evaluableSetRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, EvaluableSetRTMConsumable evaluableSetRTMConsumable) {
        final RTMClientContext producingRTMClientContext = evaluableSetRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            EvaluableSetBroadcastMessage message = evaluableSetRTMConsumable.getContent();

            final String broadcastType = evaluableSetRTMConsumable.getBroadcastType();
            final UUID subscriptionId = evaluableSetRTMConsumable.getSubscriptionId();
            final EvaluableSetRTMEventDecoratorImpl rtmEventDecorator = new EvaluableSetRTMEventDecoratorImpl(
                    new EvaluableSetRTMEvent());

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", message.getElementType())
                    .addField("evaluationMode", message.getEvaluationMode())
                    // TODO remove next line when FE supported
                    .addField("action", rtmEventDecorator.getLegacyName())
                    .addField("rtmEvent", rtmEventDecorator.getName(message.getElementType())));
        }
    }

}
