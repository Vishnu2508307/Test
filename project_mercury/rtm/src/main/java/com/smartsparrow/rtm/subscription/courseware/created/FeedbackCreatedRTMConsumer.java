package com.smartsparrow.rtm.subscription.courseware.created;

import static com.smartsparrow.courseware.data.CoursewareElementType.FEEDBACK;

import java.util.UUID;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.FeedbackCreatedBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the activity feedback created RTM event
 */
public class FeedbackCreatedRTMConsumer implements RTMConsumer<FeedbackCreatedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new FeedbackCreatedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the created elementId, componentId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param feedbackCreatedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, FeedbackCreatedRTMConsumable feedbackCreatedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = feedbackCreatedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            FeedbackCreatedBroadcastMessage message = feedbackCreatedRTMConsumable.getContent();

            final String broadcastType = feedbackCreatedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = feedbackCreatedRTMConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", FEEDBACK)
                    // TODO remove next line when FE supported
                    .addField("action", getRTMEvent().getLegacyName())
                    .addField("rtmEvent", getRTMEvent().getName()));
        }
    }

}
