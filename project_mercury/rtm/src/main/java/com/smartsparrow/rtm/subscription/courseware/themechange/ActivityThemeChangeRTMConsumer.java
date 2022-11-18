package com.smartsparrow.rtm.subscription.courseware.themechange;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;

import java.util.UUID;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.message.ConfigChangeBroadcastMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the activity theme change RTM event
 */
public class ActivityThemeChangeRTMConsumer implements RTMConsumer<ActivityThemeChangeRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new ActivityThemeChangeRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the changed activityId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param activityThemeChangeRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, ActivityThemeChangeRTMConsumable activityThemeChangeRTMConsumable) {
        final RTMClientContext producingRTMClientContext = activityThemeChangeRTMConsumable.getRTMClientContext();
        if (!rtmClient.getRtmClientContext().getClientId().equals(producingRTMClientContext.getClientId())) {
            ConfigChangeBroadcastMessage message = activityThemeChangeRTMConsumable.getContent();

            final String broadcastType = activityThemeChangeRTMConsumable.getBroadcastType();
            final UUID subscriptionId = activityThemeChangeRTMConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(broadcastType, subscriptionId.toString())
                    .addField("elementId", message.getElementId())
                    .addField("elementType", ACTIVITY)
                    .addField("theme", message.getConfig())
                    // TODO remove next line when FE supported
                    .addField("action", getRTMEvent().getLegacyName())
                    .addField("rtmEvent", getRTMEvent().getName()));
        }
    }

}
