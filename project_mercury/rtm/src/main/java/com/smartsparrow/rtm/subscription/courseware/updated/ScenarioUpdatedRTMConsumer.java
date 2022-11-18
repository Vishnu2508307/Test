package com.smartsparrow.rtm.subscription.courseware.updated;

import static com.smartsparrow.courseware.data.CoursewareElementType.SCENARIO;

import java.util.UUID;

import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.ScenarioUpdatedBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the activity scenario updated RTM event
 */
public class ScenarioUpdatedRTMConsumer implements RTMConsumer<ScenarioUpdatedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new ScenarioUpdatedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the updated activityId, scenarioId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param scenarioUpdatedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, ScenarioUpdatedRTMConsumable scenarioUpdatedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = scenarioUpdatedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            ScenarioUpdatedBroadcastMessage message = scenarioUpdatedRTMConsumable.getContent();

            final String broadcastType = scenarioUpdatedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = scenarioUpdatedRTMConsumable.getSubscriptionId();

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
