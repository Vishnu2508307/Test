package com.smartsparrow.rtm.message.handler.cohort;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.cohort.CohortGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.cohort.archived.CohortArchivedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.DateFormat;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;

import reactor.core.Exceptions;

public class ArchiveCohortMessageHandler implements MessageHandler<CohortGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ArchiveCohortMessageHandler.class);

    public static final String WORKSPACE_COHORT_ARCHIVE = "workspace.cohort.archive";
    private static final String WORKSPACE_COHORT_ARCHIVE_ERROR = "workspace.cohort.archive.error";
    private static final String WORKSPACE_COHORT_ARCHIVE_OK = "workspace.cohort.archive.ok";

    private final CohortService cohortService;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final CohortArchivedRTMProducer cohortArchivedRTMProducer;

    @Inject
    public ArchiveCohortMessageHandler(CohortService cohortService,
                                       Provider<RTMClientContext> rtmClientContextProvider,
                                       CohortArchivedRTMProducer cohortArchivedRTMProducer) {
        this.cohortService = cohortService;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.cohortArchivedRTMProducer = cohortArchivedRTMProducer;
    }

    @Override
    public void validate(CohortGenericMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getCohortId() != null, "cohortId is required");
        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), WORKSPACE_COHORT_ARCHIVE_ERROR);
        }
    }

    @Override
    public void handle(Session session, CohortGenericMessage message) throws WriteResponseException {
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();

        BasicResponseMessage responseMessage = new BasicResponseMessage(WORKSPACE_COHORT_ARCHIVE_OK, message.getId())
                .addField("finishedDate", DateFormat.asRFC1123(cohortService.archive(message.getCohortId())
                        .doOnEach(log.reactiveErrorThrowable("error while archiving cohort", throwable -> new HashMap<String, Object>() {
                            {
                                put("cohortId", message.getCohortId());
                                put("error", throwable.getStackTrace());
                            }
                        }))
                        .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                        .doOnError(throwable -> {
                            log.jsonDebug("error while archiving cohort", new HashMap<String, Object>(){
                                {
                                    put("cohortId", message.getCohortId());
                                    put("error", throwable.getStackTrace());
                                }
                            });
                            throw Exceptions.propagate(throwable);
                        })
                        .block()));

        Responses.write(session, responseMessage);
        cohortArchivedRTMProducer.buildCohortArchivedRTMConsumable(rtmClientContext, message.getCohortId())
                .produce();
    }

}
