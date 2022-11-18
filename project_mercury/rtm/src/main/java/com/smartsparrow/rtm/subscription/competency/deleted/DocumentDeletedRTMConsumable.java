package com.smartsparrow.rtm.subscription.competency.deleted;

import com.smartsparrow.competency.eventmessage.CompetencyDocumentBroadcastMessage;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.subscription.competency.CompetencyDocumentEventRTMSubscription;
import com.smartsparrow.rtm.subscription.data.AbstractRTMConsumable;

public class DocumentDeletedRTMConsumable extends AbstractRTMConsumable<CompetencyDocumentBroadcastMessage> {

    private static final long serialVersionUID = 7839018328807951021L;

    protected DocumentDeletedRTMConsumable(final RTMClientContext rtmClientContext,
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
        return new DocumentDeletedRTMEvent();
    }
}
