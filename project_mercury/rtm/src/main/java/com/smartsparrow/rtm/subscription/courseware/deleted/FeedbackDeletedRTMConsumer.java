package com.smartsparrow.rtm.subscription.courseware.deleted;

import static com.smartsparrow.courseware.data.CoursewareElementType.FEEDBACK;

import java.util.UUID;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the activity feedback deleted RTM event
 */
public class FeedbackDeletedRTMConsumer implements RTMConsumer<FeedbackDeletedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new FeedbackDeletedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the deleted activityId, componentId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param feedbackDeletedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, FeedbackDeletedRTMConsumable feedbackDeletedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = feedbackDeletedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            ActivityBroadcastMessage message = feedbackDeletedRTMConsumable.getContent();

            final String broadcastType = feedbackDeletedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = feedbackDeletedRTMConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", FEEDBACK)
                    // TODO remove next line when FE supported
                    .addField("action", getRTMEvent().getLegacyName())
                    .addField("rtmEvent", getRTMEvent().getName()));
        }
    }

}
