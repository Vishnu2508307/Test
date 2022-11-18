package com.smartsparrow.rtm.subscription.courseware.configchange;

import static com.smartsparrow.courseware.data.CoursewareElementType.FEEDBACK;

import java.util.UUID;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.ConfigChangeBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the feedback configChange RTM event
 */
public class FeedbackConfigChangeRTMConsumer implements RTMConsumer<FeedbackConfigChangeRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new FeedbackConfigChangeRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the changed feedbackId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param feedbackConfigChangeRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, FeedbackConfigChangeRTMConsumable feedbackConfigChangeRTMConsumable) {
        final RTMClientContext producingRTMClientContext = feedbackConfigChangeRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            ConfigChangeBroadcastMessage message = feedbackConfigChangeRTMConsumable.getContent();

            final String broadcastType = feedbackConfigChangeRTMConsumable.getBroadcastType();
            final UUID subscriptionId = feedbackConfigChangeRTMConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", FEEDBACK)
                    .addField("config", message.getConfig())
                    // TODO remove next line when FE supported
                    .addField("action", getRTMEvent().getLegacyName())
                    .addField("rtmEvent", getRTMEvent().getName()));
        }
    }

}
