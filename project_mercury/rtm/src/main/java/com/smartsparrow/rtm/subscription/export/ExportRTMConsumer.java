package com.smartsparrow.rtm.subscription.export;

import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.export.service.ExportService;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.export.subscription.ExportBroadcastMessage;
import com.smartsparrow.export.subscription.ExportConsumable;
import com.smartsparrow.export.subscription.ExportRTMEvent;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;

public class ExportRTMConsumer implements RTMConsumer<ExportConsumable> {

    private ExportService exportService;

    @Inject
    public ExportRTMConsumer(final ExportService exportService) {
        this.exportService = exportService;
    }

    @Override
    public RTMEvent getRTMEvent() {
        return new ExportRTMEvent();
    }

    @Override
    public void accept(final RTMClient rtmClient, final ExportConsumable exportConsumable) {

        exportService.findById(exportConsumable.getContent().getExportId()).subscribe(exportSummary -> {

            ExportBroadcastMessage msg = exportConsumable.getContent();
            final String broadcastType = exportConsumable.getBroadcastType();
            final UUID subscriptionId = exportConsumable.getSubscriptionId();

            Responses.writeReactive(rtmClient.getSession(),
                                    new BasicResponseMessage(broadcastType, subscriptionId.toString())
                                            .addField("exportId", msg.getExportId())
                                            .addField("progress", msg.getExportProgress())
                                            .addField("ambrosiaUrl", exportSummary.getAmbrosiaUrl())
                                            .addField("rtmEvent", getRTMEvent().getName()));
        });
    }
}
