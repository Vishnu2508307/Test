package com.smartsparrow.rtm.subscription.courseware.manualgrading;

import static com.smartsparrow.courseware.data.CoursewareElementType.COMPONENT;

import java.util.UUID;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.ComponentManualGradingBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the activity component manual grading configuration created RTM event
 */
public class ComponentConfigurationCreatedRTMConsumer implements RTMConsumer<ComponentConfigurationCreatedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new ComponentConfigurationCreatedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the created activityId, componentId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param componentConfigurationCreatedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient,
                       ComponentConfigurationCreatedRTMConsumable componentConfigurationCreatedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = componentConfigurationCreatedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            ComponentManualGradingBroadcastMessage message = componentConfigurationCreatedRTMConsumable.getContent();

            final String broadcastType = componentConfigurationCreatedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = componentConfigurationCreatedRTMConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(),
                                    new BasicResponseMessage(broadcastType, subscriptionId.toString())
                                            .addField("elementId", message.getElementId())
                                            .addField("elementType", COMPONENT)
                                            .addField("manualGradingConfiguration",
                                                      message.getManualGradingConfig())
                                            // TODO remove next line when FE supported
                                            .addField("action", getRTMEvent().getLegacyName())
                                            .addField("rtmEvent", getRTMEvent().getName()));
        }
    }

}
