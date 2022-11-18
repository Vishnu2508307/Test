package com.smartsparrow.rtm.message.handler.courseware.pathway;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.pathway.GetPathwayMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class GetPathwayMessageHandler implements MessageHandler<GetPathwayMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GetPathwayMessageHandler.class);

    public static final String AUTHOR_PATHWAY_GET = "author.pathway.get";
    private static final String AUTHOR_PATHWAY_GET_OK = "author.pathway.get.ok";
    private static final String AUTHOR_PATHWAY_GET_ERROR = "author.pathway.get.error";

    private final PathwayService pathwayService;

    @Inject
    public GetPathwayMessageHandler(PathwayService pathwayService) {
        this.pathwayService = pathwayService;
    }

    @Override
    public void validate(GetPathwayMessage message) throws RTMValidationException {
        affirmArgument(message.getPathwayId() != null, "pathwayId is missing");
        affirmArgument(pathwayService.findById(message.getPathwayId()).block() != null, "Pathway not found");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_PATHWAY_GET)
    @Override
    public void handle(Session session, GetPathwayMessage message) throws WriteResponseException {
        pathwayService.getPathwayPayload(message.getPathwayId())
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscribe(payload -> {
                    Responses.writeReactive(session, new BasicResponseMessage(AUTHOR_PATHWAY_GET_OK, message.getId())
                            .addField("pathway", payload));
                }, ex -> {
                    log.debug("error fetching pathway {} ", new HashMap<String,Object>() {
                        {
                            put("messageId", message.getId() );
                            put("message", message.toString());
                            put("errorCode", AUTHOR_PATHWAY_GET_ERROR);
                            put("Exception ",ex);
                        }
                    });
                    Responses.errorReactive(session, message.getId(), AUTHOR_PATHWAY_GET_ERROR,
                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "error while fetching pathway %s",
                            message.getPathwayId());
                });
    }
}
