package com.smartsparrow.rtm.subscription.competency.updated;

import com.smartsparrow.pubsub.data.RTMEvent;

public class DocumentUpdatedRTMEvent  implements RTMEvent {
    @Override
    public String getName() {
        return "DOCUMENT_UPDATED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return "DOCUMENT_UPDATED";
    }
}
