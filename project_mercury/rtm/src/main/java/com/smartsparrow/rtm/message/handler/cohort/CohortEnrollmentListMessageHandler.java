package com.smartsparrow.rtm.message.handler.cohort;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.cohort.payload.CohortEnrollmentPayload;
import com.smartsparrow.cohort.service.CohortEnrollmentService;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.cohort.CohortGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;

import reactor.core.Exceptions;

public class CohortEnrollmentListMessageHandler implements MessageHandler<CohortGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CohortEnrollmentListMessageHandler.class);

    public static final String WORKSPACE_COHORT_ENROLLMENT_LIST = "workspace.cohort.enrollment.list";
    static final String WORKSPACE_COHORT_ENROLLMENT_LIST_ERROR = "workspace.cohort.enrollment.list.error";
    static final String WORKSPACE_COHORT_ENROLLMENT_LIST_OK = "workspace.cohort.enrollment.list.ok";

    private final CohortService cohortService;
    private final CohortEnrollmentService cohortEnrollmentService;

    @Inject
    public CohortEnrollmentListMessageHandler(CohortService cohortService,
                                              CohortEnrollmentService cohortEnrollmentService) {
        this.cohortService = cohortService;
        this.cohortEnrollmentService = cohortEnrollmentService;
    }

    @Override
    public void handle(Session session, CohortGenericMessage message) throws WriteResponseException {
        List<CohortEnrollmentPayload> all = cohortEnrollmentService.fetchEnrollments(message.getCohortId())
                .flatMap(cohortEnrollmentService::getCohortEnrollmentPayload)
                .collectList()
                .doOnEach(log.reactiveErrorThrowable("error while listing cohort enrollments for cohort", throwable -> new HashMap<String, Object>() {
                    {
                        put("cohortId", message.getCohortId());
                        put("error", throwable.getStackTrace());
                    }
                }))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .doOnError(throwable -> {
                    throw Exceptions.propagate(throwable);
                })
                .block();

        emitSuccess(session, message, all);
    }

    private void emitSuccess(Session session, CohortGenericMessage message, List<CohortEnrollmentPayload> all) throws WriteResponseException {
        BasicResponseMessage response = new BasicResponseMessage(WORKSPACE_COHORT_ENROLLMENT_LIST_OK, message.getId());
        response.addField("enrollments", all);
        Responses.write(session, response);
    }

    @Override
    public void validate(CohortGenericMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getCohortId() != null, "cohortId is required");

            checkArgument(cohortService.fetchCohortSummary(message.getCohortId()).block() != null,
                    String.format("cohort not found for id %s", message.getCohortId()));

        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), WORKSPACE_COHORT_ENROLLMENT_LIST_ERROR);
        }
    }
}
