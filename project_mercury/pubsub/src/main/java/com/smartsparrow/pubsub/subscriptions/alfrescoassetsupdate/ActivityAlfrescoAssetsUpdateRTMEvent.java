package com.smartsparrow.pubsub.subscriptions.alfrescoassetsupdate;

import com.smartsparrow.pubsub.data.RTMEvent;

public class ActivityAlfrescoAssetsUpdateRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "ACTIVITY_ALFRESCO_ASSETS_UPDATE";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "ALFRESCO_ASSETS_UPDATE";
    }
}
