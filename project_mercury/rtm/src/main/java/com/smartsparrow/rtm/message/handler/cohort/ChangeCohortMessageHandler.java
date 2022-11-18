package com.smartsparrow.rtm.message.handler.cohort;

import static com.google.common.base.Preconditions.checkArgument;
import static com.smartsparrow.util.Warrants.affirmArgument;
import static com.smartsparrow.util.Warrants.affirmArgumentNotNullOrEmpty;
import static com.smartsparrow.util.Warrants.affirmValidDate;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.google.common.base.Strings;
import com.google.inject.Provider;
import com.smartsparrow.cohort.data.CohortSettings;
import com.smartsparrow.cohort.data.CohortSummary;
import com.smartsparrow.cohort.data.EnrollmentType;
import com.smartsparrow.cohort.eventmessage.CohortSummaryBroadcastMessage;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.cohort.service.LtiConsumerCredential;
import com.smartsparrow.cohort.wiring.LTIConfig;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.cohort.ChangeCohortMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.cohort.changed.CohortChangedRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.DateFormat;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.workspace.data.Workspace;
import com.smartsparrow.workspace.service.WorkspaceService;

import reactor.core.publisher.Mono;

public class ChangeCohortMessageHandler implements MessageHandler<ChangeCohortMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ChangeCohortMessageHandler.class);

    public static final String WORKSPACE_COHORT_CHANGE = "workspace.cohort.change";
    public static final String WORKSPACE_COHORT_CHANGE_OK = "workspace.cohort.change.ok";
    public static final String WORKSPACE_COHORT_CHANGE_ERROR = "workspace.cohort.change.error";

    private final CohortService cohortService;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;
    private final WorkspaceService workspaceService;
    private final CohortChangedRTMProducer cohortChangedRTMProducer;
    private final LTIConfig ltiConfig;

    @Inject
    public ChangeCohortMessageHandler(final CohortService cohortService,
                                      final Provider<RTMClientContext> rtmClientContextProvider,
                                      final Provider<RTMEventBroker> rtmEventBrokerProvider,
                                      final WorkspaceService workspaceService,
                                      final CohortChangedRTMProducer cohortChangedRTMProducer,
                                      final LTIConfig ltiConfig) {
        this.cohortService = cohortService;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
        this.workspaceService = workspaceService;
        this.cohortChangedRTMProducer = cohortChangedRTMProducer;
        this.ltiConfig = ltiConfig;
    }

    @Override
    public void validate(ChangeCohortMessage message) throws RTMValidationException {
        if (message.getStartDate() != null) {
            affirmValidDate(message.getStartDate(), "Invalid startDate");
        }

        if (message.getEndDate() != null) {
            affirmValidDate(message.getEndDate(), "Invalid endDate");
        }

        try {
            checkArgument(message.getCohortId() != null, "cohortId is required");
            checkArgument(!Strings.isNullOrEmpty(message.getName()), "name is required");
            checkArgument(message.getEnrollmentType() != null, "enrollmentType is required");

            affirmArgument(EnrollmentType.allowedFromUser(message.getEnrollmentType()), "enrollmentType not supported");

            if (message.getProductId() != null) {
                affirmArgumentNotNullOrEmpty(message.getProductId(), "productId must not be empty");
            }

            if (message.getEnrollmentType() == EnrollmentType.LTI) {
                checkArgument(message.getWorkspaceId() != null, "workspaceId is required");

                Workspace workspace = workspaceService.fetchById(message.getWorkspaceId()).block();
                checkArgument(workspace != null, "workspace not found for id " + message.getWorkspaceId());

                checkArgument(message.getLtiConsumerCredential() != null, "LTI consumer credential object is required");
                checkArgument(message.getLtiConsumerCredential().getKey() != null, "consumer key is required");
                checkArgument(message.getLtiConsumerCredential().getSecret() != null, "consumer secret is required");
            }

        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), WORKSPACE_COHORT_CHANGE_ERROR);
        }
    }

    @Override
    public void handle(Session session, ChangeCohortMessage message) throws WriteResponseException {
        RTMClientContext rtmClientContext = rtmClientContextProvider.get();
        final RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();

        final Long startDate = message.getStartDate() != null ? DateFormat.fromRFC1123(message.getStartDate()) : null;
        final Long endDate = message.getEndDate() != null ? DateFormat.fromRFC1123(message.getEndDate()) : null;

        CohortSummary summary = new CohortSummary()
                .setId(message.getCohortId())
                .setName(message.getName())
                .setType(message.getEnrollmentType())
                .setStartDate(startDate)
                .setEndDate(endDate)
                .setWorkspaceId(message.getWorkspaceId());
        // don't allow an update of the subscription id.

        CohortSettings settings = new CohortSettings()
                .setCohortId(message.getCohortId())
                .setBannerImage(message.getBannerImage())
                .setBannerPattern(message.getBannerPattern())
                .setColor(message.getColor())
                .setProductId(message.getProductId());

        cohortService.updateCohort(summary, settings)
                .then(addLtiConsumerCredential(summary, message.getLtiConsumerCredential()))
                .then(cohortService.getCohortPayload(message.getCohortId()))
                .doOnEach(log.reactiveErrorThrowable(WORKSPACE_COHORT_CHANGE_ERROR,
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("id", message.getId());
                                                             put("cohortId", message.getCohortId());
                                                         }
                                                     }))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscribe(cohortPayload -> {
                    BasicResponseMessage basicResponseMessage = new BasicResponseMessage(WORKSPACE_COHORT_CHANGE_OK,
                                                                                         message.getId());
                    basicResponseMessage.addField("cohort", cohortPayload);
                    Responses.writeReactive(session, basicResponseMessage);

                    cohortChangedRTMProducer.buildCohortChangedRTMConsumable(rtmClientContext, message.getCohortId())
                            .produce();

                   rtmEventBroker.broadcast(WORKSPACE_COHORT_CHANGE, new CohortSummaryBroadcastMessage(summary.getId())
                            .setCohortSummary(summary));
                }, ex -> {
                    log.jsonError(WORKSPACE_COHORT_CHANGE_ERROR, new HashMap<String, Object>() {
                        {
                            put("id", message.getId());
                            put("cohortId", message.getCohortId());
                            put("error", ex.getStackTrace());
                        }
                    }, ex);

                    Responses.errorReactive(session, message.getId(), WORKSPACE_COHORT_CHANGE_ERROR,
                                            HttpStatus.SC_BAD_REQUEST, WORKSPACE_COHORT_CHANGE_ERROR);
                });
    }

    private Mono<CohortSummary> addLtiConsumerCredential(CohortSummary summary, LtiConsumerCredential credential) {
        if (summary.getType() == EnrollmentType.LTI) {
            return cohortService.fetchLtiConsumerKeys(summary.getWorkspaceId(), summary.getId()).flatMap(credentials -> {
                // only save consumer credential if there is no credential for the cohort
                if (credentials.size() == 0) {
                    //Use the LTI key/secret from config if its available otherwise use from RTM request
                     return cohortService.saveLTIConsumerKey(summary, StringUtils.isNotBlank(ltiConfig.getKey()) ? ltiConfig.getKey() : credential.getKey(),
                                                             StringUtils.isNotBlank(ltiConfig.getSecret()) ? ltiConfig.getSecret() : credential.getSecret());
                }
                return Mono.empty();
            });
        }
        return Mono.empty();
    }

}
