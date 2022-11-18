package com.smartsparrow.rtm.subscription.courseware.annotationupdated;

import java.util.UUID;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.AnnotationBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the annotation updated RTM event
 */
public class AnnotationUpdatedRTMConsumer implements RTMConsumer<AnnotationUpdatedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new AnnotationUpdatedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the updated elementId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param annotationUpdatedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, AnnotationUpdatedRTMConsumable annotationUpdatedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = annotationUpdatedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            AnnotationBroadcastMessage message = annotationUpdatedRTMConsumable.getContent();

            final String broadcastType = annotationUpdatedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = annotationUpdatedRTMConsumable.getSubscriptionId();
            final AnnotationUpdatedRTMEventDecoratorImpl rtmEventDecorator = new AnnotationUpdatedRTMEventDecoratorImpl(
                    new AnnotationUpdatedRTMEvent());

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", message.getElementType())
                    .addField("annotationId", message.getAnnotationId())
                    // TODO remove next line when FE supported
                    .addField("action", rtmEventDecorator.getLegacyName())
                    .addField("rtmEvent", rtmEventDecorator.getName(message.getElementType())));
        }
    }

}
