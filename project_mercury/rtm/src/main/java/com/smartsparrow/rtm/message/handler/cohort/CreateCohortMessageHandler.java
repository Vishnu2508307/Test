package com.smartsparrow.rtm.message.handler.cohort;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmValidDate;

import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.common.base.Strings;
import com.google.inject.Provider;
import com.smartsparrow.cohort.data.EnrollmentType;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.cohort.service.LtiConsumerCredential;
import com.smartsparrow.cohort.wiring.LTIConfig;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.cohort.CreateCohortMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.DateFormat;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.workspace.data.Workspace;
import com.smartsparrow.workspace.service.WorkspaceService;

import reactor.core.publisher.Mono;

public class CreateCohortMessageHandler implements MessageHandler<CreateCohortMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CreateCohortMessageHandler.class);

    public final static String WORKSPACE_COHORT_CREATE = "workspace.cohort.create";
    private final static String WORKSPACE_COHORT_CREATE_OK = "workspace.cohort.create.ok";
    private final static String WORKSPACE_COHORT_CREATE_ERROR = "workspace.cohort.create.error";

    private final Provider<AuthenticationContext> authenticationContextProvider;
    private final CohortService cohortService;
    private final WorkspaceService workspaceService;
    private final LTIConfig ltiConfig;

    @Inject
    public CreateCohortMessageHandler(Provider<AuthenticationContext> authenticationContextProvider,
                                      CohortService cohortService,
                                      WorkspaceService workspaceService,
                                      final LTIConfig ltiConfig) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.cohortService = cohortService;
        this.workspaceService = workspaceService;
        this.ltiConfig = ltiConfig;
    }

    @Override
    public void validate(CreateCohortMessage message) throws RTMValidationException {

        final String startDate = message.getStartDate();
        final String endDate = message.getEndDate();

        if (startDate != null) {
            affirmValidDate(startDate, "Invalid startDate");
        }

        if (endDate != null) {
            affirmValidDate(endDate, "Invalid endDate");
        }

        try {
            checkArgument(!Strings.isNullOrEmpty(message.getName()), "name is required");
            checkArgument(message.getEnrollmentType() != null, "enrollmentType is required");
            checkArgument(message.getWorkspaceId() != null, "workspaceId is required");

            affirmArgument(EnrollmentType.allowedFromUser(message.getEnrollmentType()), "enrollmentType not supported");

            Workspace workspace = workspaceService.fetchById(message.getWorkspaceId()).block();

            checkArgument(workspace != null, "workspace not found for id " + message.getWorkspaceId());

            if (message.getEnrollmentType() == EnrollmentType.LTI) {
                checkArgument(message.getLtiConsumerCredential() != null, "LTI consumer credential object is required");
                checkArgument(message.getLtiConsumerCredential().getKey() != null, "consumer key is required");
                checkArgument(message.getLtiConsumerCredential().getSecret() != null, "consumer secret is required");
            }

        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), WORKSPACE_COHORT_CREATE_ERROR);
        }

    }

    @Override
    public void handle(Session session, CreateCohortMessage message) throws WriteResponseException {

        UUID accountId = authenticationContextProvider.get().getAccount().getId();

        final Long startDate = message.getStartDate() != null ? DateFormat.fromRFC1123(message.getStartDate()) : null;
        final Long endDate = message.getEndDate() != null ? DateFormat.fromRFC1123(message.getEndDate()) : null;

        // find the workspace
        workspaceService.fetchById(message.getWorkspaceId())
                // create a cohort using the supplied parameters
                .flatMap(workspace -> cohortService.createCohort(accountId, //
                                                                 message.getWorkspaceId(), //
                                                                 message.getName(), //
                                                                 message.getEnrollmentType(), //
                                                                 startDate, //
                                                                 endDate, //
                                                                 workspace.getSubscriptionId()))
                // create the cohort settings
                .flatMap(cohortSummary -> cohortService.createSettings(cohortSummary.getId(), //
                                                                       message.getBannerPattern(), //
                                                                       message.getColor(), //
                                                                       message.getBannerImage(), //
                                                                       message.getProductId()) //
                        .thenReturn(cohortSummary))
                // try saving the LTI consumer key and secret to this cohort
                .flatMap(cohortSummary -> {
                    // when the type is LTI save the key to this cohort
                    if (cohortSummary.getType().equals(EnrollmentType.LTI)) {
                        final LtiConsumerCredential creds = message.getLtiConsumerCredential();
                        //Use the LTI key/secret from config if its available otherwise use from RTM request
                        return cohortService.saveLTIConsumerKey(cohortSummary, StringUtils.isNotBlank(ltiConfig.getKey()) ? ltiConfig.getKey() : creds.getKey(),
                                                                StringUtils.isNotBlank(ltiConfig.getSecret()) ? ltiConfig.getSecret() : creds.getSecret());
                    }
                    // otherwise, do nothing
                    return Mono.just(cohortSummary);
                })
                // map it to a wire payload
                .flatMap(cohortSummary -> cohortService.getCohortPayload(cohortSummary.getId()))
                //
                .doOnEach(log.reactiveDebug("error while creating the cohort"))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscribe(payload -> {
                    BasicResponseMessage basicResponseMessage = new BasicResponseMessage(WORKSPACE_COHORT_CREATE_OK, message.getId());
                    basicResponseMessage.addField("cohort", payload);
                    Responses.writeReactive(session, basicResponseMessage);
                }, ex -> {
                    Responses.errorReactive(session, message.getId(), WORKSPACE_COHORT_CREATE_ERROR,
                                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "error while creating the cohort");
                });

    }

}
