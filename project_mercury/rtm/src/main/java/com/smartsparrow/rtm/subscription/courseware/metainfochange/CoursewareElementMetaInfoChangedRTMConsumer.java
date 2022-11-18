package com.smartsparrow.rtm.subscription.courseware.metainfochange;

import java.util.UUID;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.ActivityBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the courseware element meta info RTM event
 */
public class CoursewareElementMetaInfoChangedRTMConsumer implements RTMConsumer<CoursewareElementMetaInfoChangedRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new CoursewareElementMetaInfoChangedRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the changed elementId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param coursewareElementMetaInfoChangedRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, CoursewareElementMetaInfoChangedRTMConsumable coursewareElementMetaInfoChangedRTMConsumable) {
        final RTMClientContext producingRTMClientContext = coursewareElementMetaInfoChangedRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            ActivityBroadcastMessage message = coursewareElementMetaInfoChangedRTMConsumable.getContent();

            final String broadcastType = coursewareElementMetaInfoChangedRTMConsumable.getBroadcastType();
            final UUID subscriptionId = coursewareElementMetaInfoChangedRTMConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", message.getElementType())
                    // TODO remove next line when FE supported
                    .addField("action", getRTMEvent().getLegacyName())
                    .addField("rtmEvent", getRTMEvent().getName()));
        }
    }

}
