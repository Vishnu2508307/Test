package com.smartsparrow.rtm.subscription.competency.association.deleted;

import com.smartsparrow.pubsub.data.RTMEvent;

public class CompetencyItemAssociationDeletedRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "COMPETENCY_ASSOCIATION_DELETED";
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
        return "ASSOCIATION_DELETED";
    }
}
