package com.smartsparrow.rtm.subscription.courseware.alfrescoassetsupdate;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;

import java.util.UUID;

import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.pubsub.subscriptions.alfrescoassetsupdate.ActivityAlfrescoAssetsUpdateRTMConsumable;
import com.smartsparrow.pubsub.subscriptions.alfrescoassetsupdate.ActivityAlfrescoAssetsUpdateRTMEvent;
import com.smartsparrow.pubsub.subscriptions.alfrescoassetsupdate.AlfrescoAssetsUpdateBroadcastMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

/**
 * Consumer that handles the activity alfresco assets updated RTM event
 */
public class ActivityAlfrescoAssetsUpdateRTMConsumer implements RTMConsumer<ActivityAlfrescoAssetsUpdateRTMConsumable> {

    @Override
    public RTMEvent getRTMEvent() {
        return new ActivityAlfrescoAssetsUpdateRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the updated assetId and the action
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param activityAlfrescoAssetsUpdateRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient,
                       ActivityAlfrescoAssetsUpdateRTMConsumable activityAlfrescoAssetsUpdateRTMConsumable) {

        AlfrescoAssetsUpdateBroadcastMessage message = activityAlfrescoAssetsUpdateRTMConsumable.getContent();

        final String broadcastType = activityAlfrescoAssetsUpdateRTMConsumable.getBroadcastType();
        final UUID subscriptionId = activityAlfrescoAssetsUpdateRTMConsumable.getSubscriptionId();

        Responses.writeReactive(rtmClient.getSession(),
                                new BasicResponseMessage(broadcastType, subscriptionId.toString())
                                        .addField("elementId", message.getElementId())
                                        .addField("elementType", ACTIVITY)
                                        .addField("assetId", message.getAssetId())
                                        .addField("alfrescoSyncType", message.getAlfrescoSyncType())
                                        .addField("isAlfrescoAssetUpdated", message.isAlfrescoAssetUpdated())
                                        .addField("isAlfrescoSyncComplete", message.isAlfrescoSyncComplete())
                                        // TODO remove next line when FE supported
                                        .addField("action", getRTMEvent().getLegacyName())
                                        .addField("rtmEvent", getRTMEvent().getName()));

    }

}
