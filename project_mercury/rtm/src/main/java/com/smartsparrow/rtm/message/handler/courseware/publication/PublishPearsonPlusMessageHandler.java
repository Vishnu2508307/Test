package com.smartsparrow.rtm.message.handler.courseware.publication;

import com.smartsparrow.cohort.data.EnrollmentType;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.publication.PublishPearsonPlusMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.workspace.service.WorkspaceService;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import reactor.core.Exceptions;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.HashMap;
import java.util.UUID;

import static com.smartsparrow.util.Warrants.affirmArgument;

public class PublishPearsonPlusMessageHandler implements MessageHandler<PublishPearsonPlusMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(PublishPearsonPlusMessageHandler.class);

    public static final String PUBLICATION_PEARSON_PLUS_PUBLISH_REQUEST = "publication.pearsonplus.publish.request";
    public static final String PUBLICATION_PEARSON_PLUS_PUBLISH_REQUEST_OK= "publication.pearsonplus.publish.request.ok";
    public static final String PUBLICATION_PEARSON_PLUS_PUBLISH_REQUEST_ERROR = "publication.pearsonplus.publish.request.error";


    private final Provider<AuthenticationContext> authenticationContextProvider;
    private final DeploymentService deploymentService;
    private final WorkspaceService workspaceService;
    private final CohortService cohortService;
    private final ActivityService activityService;


    @Inject
    public PublishPearsonPlusMessageHandler(final Provider<AuthenticationContext> authenticationContextProvider,
                                              final DeploymentService deploymentService,
                                              final WorkspaceService workspaceService,
                                              final CohortService cohortService,
                                              final ActivityService activityService) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.deploymentService = deploymentService;
        this.workspaceService = workspaceService;
        this.cohortService = cohortService;
        this.activityService = activityService;
    }



    @Override
    public void validate(PublishPearsonPlusMessage message) throws RTMValidationException {
        affirmArgument(message.getActivityId() != null, "activity id is required");
        affirmArgument(activityService.findById(message.getActivityId()).block() != null, "Activity not found");
        affirmArgument(message.getProductId() != null, "product id is required");
    }

    @Override
    public void handle(Session session, PublishPearsonPlusMessage message) throws WriteResponseException {

        UUID accountId = authenticationContextProvider.get().getAccount().getId();

        String productId = message.getProductId();

        UUID deploymentId = deploymentService.findProductDeploymentId(productId).block();
        UUID newUUID = UUIDs.timeBased();
        UUID cohortId = newUUID;

        DeployedActivity deployedActivity = (deploymentId == null) ?
                null : deploymentService.findLatestDeploymentOrEmpty(message.getActivityId(), deploymentId).block();

        if (deployedActivity != null) {

            cohortId = deployedActivity.getCohortId();

        } else { // first time deploying/publishing this courseware for classes PEARSON PLUS, create cohort (template)

            // todo how are start and end dates set for classes pearson plus?  By MX?
            final Long startDate = null;
            final Long endDate = null;

            // find the workspace
            UUID newCohortId = cohortId;
            activityService.findWorkspaceIdByActivity(message.getActivityId())
                    .flatMap(workspaceId -> workspaceService.fetchById(workspaceId))
                    // create a cohort using the supplied parameters
                    .flatMap(workspace -> cohortService.createCohort(newCohortId,
                            accountId,
                            workspace.getId(),
                            "Pearson Plus Cohort",
                            EnrollmentType.OPEN,
                            startDate,
                            endDate,
                            workspace.getSubscriptionId()))
                    // create the cohort settings
                    .flatMap(cohortSummary -> cohortService.createSettings(cohortSummary.getId(),
                                    null, null, null,
                                    productId) //
                            .thenReturn(cohortSummary))
                    // associate product id to cohort id
                    .flatMap(cohortSummary -> cohortService.saveProductCohortId(productId, newCohortId)
                            .thenReturn(cohortSummary))
                    .doOnEach(log.reactiveInfo("creating new pearson plus template with id %s"))
                    .doOnError(throwable -> {
                        log.reactiveError("error while creating new pearson plus cohort template");

                        Responses.errorReactive(session, message.getId(), PUBLICATION_PEARSON_PLUS_PUBLISH_REQUEST_ERROR,
                                HttpStatus.SC_UNPROCESSABLE_ENTITY, "error while creating new pearson plus cohort template");

                        throw Exceptions.propagate(throwable);
                    })
                    .block();

        }

        // return OK case at this stage, deploy stage (below) will broadcast deployment success/failure asynchronously
        Responses.writeReactive(session,
                new BasicResponseMessage(PUBLICATION_PEARSON_PLUS_PUBLISH_REQUEST_OK, message.getId())
                        .addField("activityId", message.getActivityId()));

        // todo publishing progress logic will be handled in BRNT-11465
        // todo final RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();

        // deploy courseware to learnspace
        deploymentService.deploy(message.getActivityId(),
                        cohortId,
                        deploymentId,
                        true // todo requirements for classes on-demand?
                )
                .doOnEach(log.reactiveErrorThrowable("error publishing activity ", throwable -> new HashMap<String, Object>() {
                    {
                        put("activityId", message.getActivityId());
                    }
                }))
                .flatMap(deployment -> {
                    // create/update product id to deployment id
                    return deploymentService.saveProductDeploymentId(productId, deployment.getId())
                            .thenReturn(deployment);
                })
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscribe(deployment -> {
                    //log.info("deployed product %s, using cohort id and deploymentId");//cohort id  and    deploymentId   and productId

                    log.jsonInfo("deployed class pearson plus", new HashedMap<String, Object>() {
                        {
                            put("productId", productId);
                            put("cohortId", deployment.getCohortId());
                            put("deploymentId", deployment.getId());
                            put("isNewDeployment", newUUID.equals(deployment.getCohortId()));
                        }
                    });
                    // todo rtmEventBroker.broadcast(PROJECT_ACTIVITY_PUBLISH, new PublishedActivityBroadcastMessage(deployment.getCohortId())
                    // todo        .setPublishedActivity(deployment));
                }, ex -> {
                    // todo rtmEventBroker.broadcast(PROJECT_ACTIVITY_PUBLISH, new PublishedActivityBroadcastMessage(deployment.getCohortId())
                    // todo        .setPublishedActivity(deployment));
                });

    }

}
