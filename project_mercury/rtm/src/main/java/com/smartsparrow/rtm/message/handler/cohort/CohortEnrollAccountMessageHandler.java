package com.smartsparrow.rtm.message.handler.cohort;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.UUID;

import javax.inject.Inject;

import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;
import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.cohort.payload.CohortEnrollmentPayload;
import com.smartsparrow.cohort.service.CohortEnrollmentService;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.cohort.CohortEnrollmentMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.cohort.enrolled.CohortEnrolledRTMProducer;
import com.smartsparrow.rtm.util.Responses;


@Deprecated
public class CohortEnrollAccountMessageHandler implements MessageHandler<CohortEnrollmentMessage> {

    public static final String WORKSPACE_COHORT_ACCOUNT_ENROLL = "workspace.cohort.account.enroll";
    private static final String WORKSPACE_COHORT_ACCOUNT_ENROLL_ERROR = "workspace.cohort.account.enroll.error";
    private static final String WORKSPACE_COHORT_ACCOUNT_ENROLL_OK = "workspace.cohort.account.enroll.ok";

    private final CohortService cohortService;
    private final AccountService accountService;
    private final CohortEnrollmentService cohortEnrollmentService;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final AuthenticationContextProvider authenticationContextProvider;
    private final CohortEnrolledRTMProducer cohortEnrolledRTMProducer;

    @Inject
    public CohortEnrollAccountMessageHandler(CohortService cohortService,
                                             AccountService accountService,
                                             CohortEnrollmentService cohortEnrollmentService,
                                             Provider<RTMClientContext> rtmClientContextProvider,
                                             AuthenticationContextProvider authenticationContextProvider,
                                             CohortEnrolledRTMProducer cohortEnrolledRTMProducer) {
        this.cohortService = cohortService;
        this.accountService = accountService;
        this.cohortEnrollmentService = cohortEnrollmentService;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.authenticationContextProvider = authenticationContextProvider;
        this.cohortEnrolledRTMProducer = cohortEnrolledRTMProducer;
    }

    /**
     * Manual enroll an account into a cohort
     *
     * @param session the websocket session
     * @param message the newly arrived message
     * @throws WriteResponseException when failing to write message on the websocket
     */
    @Override
    public void handle(Session session, CohortEnrollmentMessage message) throws WriteResponseException {
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        UUID enrolledBy = authenticationContextProvider.get().getAccount().getId();

        CohortEnrollmentPayload payload = cohortEnrollmentService.enrollAccount(message.getAccountId(), message.getCohortId(), enrolledBy)
                .flatMap(cohortEnrollmentService::getCohortEnrollmentPayload)
                .block();

        cohortEnrolledRTMProducer.buildCohortEnrolledRTMConsumable(rtmClientContext, message.getCohortId())
                .produce();

        BasicResponseMessage response = new BasicResponseMessage(WORKSPACE_COHORT_ACCOUNT_ENROLL_OK, message.getId());
        response.addField("enrollment", payload);
        Responses.write(session, response);
    }

    /**
     * Validate that the required arguments are supplied.
     * <br> {@link CohortEnrollmentMessage#getAccountId()} is required.
     * <br> {@link CohortEnrollmentMessage#getCohortId()} is required.
     * <br> the cohort must be found.
     * <br> the account must be found.
     *
     * @param message the received message that requires to be validated
     * @throws RTMValidationException when any of the above requirement is not met
     */
    @Override
    public void validate(CohortEnrollmentMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getAccountId() != null, "accountId is required");
            checkArgument(message.getCohortId() != null, "cohortId is required");
            checkArgument(cohortService.fetchCohortSummary(message.getCohortId()).block() != null,
                    String.format("cohort not found for id %s", message.getCohortId()));
            checkArgument(accountService.findById(message.getAccountId()).blockLast() != null,
                    String.format("account not found for id %s", message.getAccountId()));
        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), WORKSPACE_COHORT_ACCOUNT_ENROLL_ERROR);
        }
    }
}
