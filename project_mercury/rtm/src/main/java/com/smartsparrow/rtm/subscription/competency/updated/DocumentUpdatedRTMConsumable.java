package com.smartsparrow.rtm.subscription.competency.updated;

import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.competency.CompetencyDocumentEventRTMSubscription;
import com.smartsparrow.rtm.subscription.data.AbstractRTMConsumable;
import com.smartsparrow.pubsub.data.RTMEvent;

public class DocumentUpdatedRTMConsumable extends AbstractRTMConsumable<CompetencyDocumentBroadcastMessage> {

    private static final long serialVersionUID = 2350043705648949084L;

    protected DocumentUpdatedRTMConsumable(final RTMClientContext rtmClientContext,
                                           final CompetencyDocumentBroadcastMessage content) {
        super(rtmClientContext, content);
    }

    @Override
    public String getName() {
        return String.format("competency.document/%s/%s", content.getDocumentId(), getRTMEvent().getName());
    }

    @Override
    public String getSubscriptionName() {
        return CompetencyDocumentEventRTMSubscription.NAME(content.getDocumentId());
    }

    @Override
    public RTMEvent getRTMEvent() {
        return new DocumentUpdatedRTMEvent();
    }
}
