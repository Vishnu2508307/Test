package com.smartsparrow.rtm.subscription.courseware.publication;

import com.smartsparrow.courseware.eventmessage.PublicationJobBroadcastMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.data.RTMConsumer;
import com.smartsparrow.pubsub.data.RTMEvent;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

/**
 * Consumer that handles the publication job event.
 */
public class PublicationJobRTMConsumer implements RTMConsumer<PublicationJobRTMConsumable> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(PublicationJobRTMConsumer.class);

    @Override
    public RTMEvent getRTMEvent() {
        return new PublicationRTMEvent();
    }

    /**
     * Writes a message to the websocket connection including the publicationId the created publicationJobStatus,
     * status message and the action.
     *
     * @param rtmClient the rtm client that is listening to this consumer
     * @param publicationJobRTMConsumable the produced consumable
     */
    @Override
    public void accept(RTMClient rtmClient, PublicationJobRTMConsumable publicationJobRTMConsumable) {
            PublicationJobBroadcastMessage message = publicationJobRTMConsumable.getContent();
            log.info("Broadcasting publication job status");
            Responses.writeReactive(rtmClient.getSession(), new BasicResponseMessage(publicationJobRTMConsumable.getBroadcastType()
                    , publicationJobRTMConsumable.getSubscriptionId().toString())
                    .addField("publicationId", message.getPublicationId())
                    .addField("publicationJobStatus", message.getPublicationJobStatus())
                    .addField("jobId", message.getJobId())
                    .addField("statusMessage", message.getStatusMessage())
                    .addField("bookId", message.getBookId())
                    .addField("etextVersion", message.getEtextVersion())
                    // TODO remove next line when FE supported
                    .addField("action", getRTMEvent().getName())
                    .addField("rtmEvent", getRTMEvent().getName()));
            log.info("Broadcasting is done for publication job status");
    }

}
