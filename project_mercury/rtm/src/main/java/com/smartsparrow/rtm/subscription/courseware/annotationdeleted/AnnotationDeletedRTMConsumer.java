package com.smartsparrow.rtm.subscription.courseware.annotationdeleted;

import java.util.UUID;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.annotationcreated.AnnotationCreatedRTMEvent;
import com.smartsparrow.rtm.subscription.courseware.annotationcreated.AnnotationCreatedRTMEventDecoratorImpl;
import com.smartsparrow.rtm.subscription.courseware.message.AnnotationBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the annotation deleted RTM event
 */
public class AnnotationDeletedRTMConsumer implements RTMConsumer<AnnotationDeletedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new AnnotationDeletedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the deleted elementId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param annotationDeletedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, AnnotationDeletedRTMConsumable annotationDeletedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = annotationDeletedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            AnnotationBroadcastMessage message = annotationDeletedRTMConsumable.getContent();

            final String broadcastType = annotationDeletedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = annotationDeletedRTMConsumable.getSubscriptionId();
            final AnnotationDeletedRTMEventDecoratorImpl rtmEventDecorator = new AnnotationDeletedRTMEventDecoratorImpl(
                    new AnnotationDeletedRTMEvent());

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
