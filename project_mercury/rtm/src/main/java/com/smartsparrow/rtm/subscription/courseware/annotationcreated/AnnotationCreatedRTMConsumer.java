package com.smartsparrow.rtm.subscription.courseware.annotationcreated;

import java.util.UUID;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.AnnotationBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the annotation created RTM event
 */
public class AnnotationCreatedRTMConsumer implements RTMConsumer<AnnotationCreatedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new AnnotationCreatedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the created elementId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param annotationCreatedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, AnnotationCreatedRTMConsumable annotationCreatedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = annotationCreatedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            AnnotationBroadcastMessage message = annotationCreatedRTMConsumable.getContent();

            final String broadcastType = annotationCreatedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = annotationCreatedRTMConsumable.getSubscriptionId();
            final AnnotationCreatedRTMEventDecoratorImpl rtmEventDecorator = new AnnotationCreatedRTMEventDecoratorImpl(
                    new AnnotationCreatedRTMEvent());

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
