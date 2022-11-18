package com.smartsparrow.rtm.subscription.courseware.created;

import static com.smartsparrow.courseware.data.CoursewareElementType.SCENARIO;

import java.util.UUID;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.ScenarioCreatedBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the activity scenario created RTM event
 */
public class ScenarioCreatedRTMConsumer implements RTMConsumer<ScenarioCreatedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new ScenarioCreatedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the created activityId, scenarioId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param scenarioCreatedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, ScenarioCreatedRTMConsumable scenarioCreatedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = scenarioCreatedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            ScenarioCreatedBroadcastMessage message = scenarioCreatedRTMConsumable.getContent();

            final String broadcastType = scenarioCreatedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = scenarioCreatedRTMConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", SCENARIO)
                    .addField("parentElementId", message.getParentElementId())
                    .addField("parentElementType", message.getParentElementType())
                    .addField("lifecycle", message.getLifecycle())
                    // TODO remove next line when FE supported
                    .addField("action", getRTMEvent().getLegacyName())
                    .addField("rtmEvent", getRTMEvent().getName()));
        }
    }

}
