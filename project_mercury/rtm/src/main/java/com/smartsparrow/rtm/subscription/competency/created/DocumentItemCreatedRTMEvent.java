package com.smartsparrow.rtm.subscription.competency.created;

import com.smartsparrow.pubsub.data.RTMEvent;

public class DocumentItemCreatedRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "DOCUMENT_ITEM_CREATED";
    }

    @Override
    public Boolean equalsTo(final RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    /**
     * This method is used to support FE action
     * FIXME: this method will be removed when FE implements with getName() action
     */
    @Override
    public String getLegacyName() {
        return "DOCUMENT_ITEM_CREATED";
    }
}
