package com.smartsparrow.export.subscription;

import com.smartsparrow.pubsub.data.AbstractConsumable;
import com.smartsparrow.pubsub.data.RTMEvent;

public class ExportConsumable extends AbstractConsumable<ExportBroadcastMessage> {

    private static final long serialVersionUID = 7531308488099167086L;

    public ExportConsumable(final ExportBroadcastMessage content) {
        super(content);
    }

    @Override
    public String getName() {
        return String.format("author.activity.export/%s/%s", content.getExportId(), getRTMEvent().getName());
    }

    @Override
    public String getSubscriptionName() {
        return String.format("author.activity.export/%s", content.getExportId());
    }

    @Override
    public RTMEvent getRTMEvent() {
        return new ExportRTMEvent();
    }
}
