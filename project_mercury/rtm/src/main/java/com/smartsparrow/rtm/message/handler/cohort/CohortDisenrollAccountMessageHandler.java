package com.smartsparrow.rtm.message.handler.cohort;

import static com.google.common.base.Preconditions.checkArgument;

import javax.inject.Inject;

import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.cohort.service.CohortEnrollmentService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.cohort.CohortEnrollmentMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.cohort.disenrolled.CohortDisEnrolledRTMProducer;
import com.smartsparrow.rtm.util.Responses;


@Deprecated
public class CohortDisenrollAccountMessageHandler implements MessageHandler<CohortEnrollmentMessage> {

    public static final String WORKSPACE_COHORT_ACCOUNT_DISENROLL = "workspace.cohort.account.disenroll";
    private static final String WORKSPACE_COHORT_ACCOUNT_DISENROLL_ERROR = "workspace.cohort.account.disenroll.error";
    private static final String WORKSPACE_COHORT_ACCOUNT_DISENROLL_OK = "workspace.cohort.account.disenroll.ok";

    private final CohortEnrollmentService cohortEnrollmentService;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final CohortDisEnrolledRTMProducer cohortDisEnrolledRTMProducer;

    @Inject
    public CohortDisenrollAccountMessageHandler(CohortEnrollmentService cohortEnrollmentService,
                                                Provider<RTMClientContext> rtmClientContextProvider,
                                                CohortDisEnrolledRTMProducer cohortDisEnrolledRTMProducer) {
        this.cohortEnrollmentService = cohortEnrollmentService;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.cohortDisEnrolledRTMProducer = cohortDisEnrolledRTMProducer;
    }

    @Override
    public void handle(Session session, CohortEnrollmentMessage message) throws WriteResponseException {
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();

        cohortEnrollmentService.disenrollAccount(message.getAccountId(), message.getCohortId()).blockLast();

        cohortDisEnrolledRTMProducer.buildCohortDisEnrolledRTMConsumable(rtmClientContext, message.getCohortId())
                .produce();

        BasicResponseMessage response = new BasicResponseMessage(WORKSPACE_COHORT_ACCOUNT_DISENROLL_OK, message.getId());
        Responses.write(session, response);
    }

    @Override
    public void validate(CohortEnrollmentMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getAccountId() != null, "accountId is required");
            checkArgument(message.getCohortId() != null, "cohortId is required");
        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), WORKSPACE_COHORT_ACCOUNT_DISENROLL_ERROR);
        }
    }
}
