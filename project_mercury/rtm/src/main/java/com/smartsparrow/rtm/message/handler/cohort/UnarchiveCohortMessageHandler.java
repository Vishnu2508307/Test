package com.smartsparrow.rtm.message.handler.cohort;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashMap;

import javax.inject.Inject;

import com.smartsparrow.rtm.subscription.cohort.unarchived.CohortUnArchivedRTMProducer;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.cohort.CohortGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;

import reactor.core.Exceptions;

public class UnarchiveCohortMessageHandler implements MessageHandler<CohortGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(UnarchiveCohortMessageHandler.class);

    public static final String WORKSPACE_COHORT_UNARCHIVE = "workspace.cohort.unarchive";
    private static final String WORKSPACE_COHORT_UNARCHIVE_ERROR = "workspace.cohort.unarchive.error";
    private static final String WORKSPACE_COHORT_UNARCHIVE_OK = "workspace.cohort.unarchive.ok";

    private final CohortService cohortService;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final CohortUnArchivedRTMProducer cohortUnArchivedRTMProducer;

    @Inject
    public UnarchiveCohortMessageHandler(CohortService cohortService,
                                         Provider<RTMClientContext> rtmClientContextProvider,
                                         CohortUnArchivedRTMProducer cohortUnArchivedRTMProducer) {
        this.cohortService = cohortService;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.cohortUnArchivedRTMProducer = cohortUnArchivedRTMProducer;
    }

    @Override
    public void validate(CohortGenericMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getCohortId() != null, "cohortId is required");
        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), WORKSPACE_COHORT_UNARCHIVE_ERROR);
        }
    }

    @Override
    public void handle(Session session, CohortGenericMessage message) throws WriteResponseException {
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();

        cohortService.unarchive(message.getCohortId())
                .doOnEach(log.reactiveErrorThrowable("error while un-archiving cohort", throwable -> new HashMap<String, Object>() {
                    {
                        put("cohortId", message.getCohortId());
                        put("error", throwable.getStackTrace());
                    }
                }))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                })
                .blockLast();
        Responses.write(session, new BasicResponseMessage(WORKSPACE_COHORT_UNARCHIVE_OK, message.getId()));

        cohortUnArchivedRTMProducer.buildCohortUnArchivedRTMConsumable(rtmClientContext, message.getCohortId())
                .produce();
    }
}
