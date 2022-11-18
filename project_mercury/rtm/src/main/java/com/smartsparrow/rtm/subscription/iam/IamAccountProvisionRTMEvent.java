package com.smartsparrow.rtm.subscription.iam;

import com.smartsparrow.pubsub.data.RTMEvent;

public class IamAccountProvisionRTMEvent implements RTMEvent {
    @Override
    public String getName() {
        return "ACCOUNT_PROVISIONED";
    }

    @Override
    public Boolean equalsTo(RTMEvent rtmEvent) {
        return getName().equals(rtmEvent.getName());
    }

    @Override
    public String getLegacyName() {
        return getName();
    }


}
