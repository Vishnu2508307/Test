package com.smartsparrow.rtm.subscription.courseware.scenarioreordered;

import java.util.UUID;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.ScenarioReOrderedBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the activity scenario reordered RTM event
 */
public class ScenarioReOrderedRTMConsumer implements RTMConsumer<ScenarioReOrderedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new ScenarioReOrderedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the scenario reordered activityId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param activityScenarioReOrderedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, ScenarioReOrderedRTMConsumable activityScenarioReOrderedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = activityScenarioReOrderedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            ScenarioReOrderedBroadcastMessage message = activityScenarioReOrderedRTMConsumable.getContent();

            final String broadcastType = activityScenarioReOrderedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = activityScenarioReOrderedRTMConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", message.getElementType())
                    .addField("scenarioIds", message.getScenarioIds())
                    .addField("lifecycle", message.getLifecycle())
                    // TODO remove next line when FE supported
                    .addField("action", getRTMEvent().getLegacyName())
                    .addField("rtmEvent", getRTMEvent().getName()));
        }
    }

}
