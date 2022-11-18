package com.smartsparrow.rtm.subscription.courseware.manualgrading;

import static com.smartsparrow.courseware.data.CoursewareElementType.COMPONENT;

import java.util.UUID;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the activity component manual grading config deleted RTM event
 */
public class ComponentManualGradingConfigDeletedRTMConsumer implements RTMConsumer<ComponentManualGradingConfigDeletedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new ComponentManualGradingConfigDeletedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the updated componentId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param componentManualGradingConfigDeletedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, ComponentManualGradingConfigDeletedRTMConsumable componentManualGradingConfigDeletedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = componentManualGradingConfigDeletedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            ActivityBroadcastMessage message = componentManualGradingConfigDeletedRTMConsumable.getContent();

            final String broadcastType = componentManualGradingConfigDeletedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = componentManualGradingConfigDeletedRTMConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", COMPONENT)
                    // TODO remove next line when FE supported
                    .addField("action", getRTMEvent().getLegacyName())
                    .addField("rtmEvent", getRTMEvent().getName()));
        }
    }

}
