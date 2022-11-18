package com.smartsparrow.rtm.message.handler.courseware.publication;

import com.smartsparrow.cohort.data.EnrollmentType;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.cohort.service.LtiConsumerCredential;
import com.smartsparrow.cohort.wiring.LTIConfig;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.publication.PublishClassOnDemandMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.workspace.data.PublicationSettings;
import com.smartsparrow.workspace.service.ProjectService;
import com.smartsparrow.workspace.service.PublishMetadataService;
import com.smartsparrow.workspace.service.WorkspaceService;
import com.smartsparrow.workspace.wiring.PublishMetadataConfig;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONObject;

import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import javax.inject.Provider;

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

import static com.smartsparrow.util.Warrants.affirmArgument;

public class PublishClassOnDemandMessageHandler implements MessageHandler<PublishClassOnDemandMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(PublishClassOnDemandMessageHandler.class);

    public static final String PUBLICATION_CLASSONDEMAND_PUBLISH_REQUEST = "publication.classondemand.publish.request";
    public static final String PUBLICATION_CLASSONDEMAND_PUBLISH_REQUEST_OK = "publication.classondemand.publish.request.ok";
    public static final String PUBLICATION_CLASSONDEMAND_PUBLISH_REQUEST_ERROR = "publication.classondemand.publish.request.error";

    private final Provider<AuthenticationContext> authenticationContextProvider;
    private final DeploymentService deploymentService;
    private final WorkspaceService workspaceService;
    private final CohortService cohortService;
    private final ActivityService activityService;
    private final ProjectService projectService;
    private final PublishMetadataService publishMetadataService;
    private final LTIConfig ltiConfig;
    private final PublishMetadataConfig publishMetadataConfig;

    @Inject
    public PublishClassOnDemandMessageHandler(final Provider<AuthenticationContext> authenticationContextProvider,
                                              final DeploymentService deploymentService,
                                              final WorkspaceService workspaceService,
                                              final CohortService cohortService,
                                              final ActivityService activityService,
                                              final ProjectService projectService,
                                              final PublishMetadataService publishMetadataService,
                                              final LTIConfig ltiConfig,
                                              final PublishMetadataConfig publishMetadataConfig) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.deploymentService = deploymentService;
        this.workspaceService = workspaceService;
        this.cohortService = cohortService;
        this.activityService = activityService;
        this.projectService = projectService;
        this.publishMetadataService = publishMetadataService;
        this.ltiConfig = ltiConfig;
        this.publishMetadataConfig = publishMetadataConfig;
    }

    @Override
    public void validate(PublishClassOnDemandMessage message) throws RTMValidationException {
        affirmArgument(message.getActivityId() != null, "activity id is required");
        affirmArgument(activityService.findById(message.getActivityId()).block() != null, "Activity not found");

        affirmArgument(message.getProductId() != null, "product id is required");
        // affirmArgument(message.getSettings() != null, "settings is required"); todo uncomment during integration with frontend
        affirmArgument(message.getLtiConsumerCredential() != null, "consumer credential is required");
    }

    @Override
    public void handle(Session session, PublishClassOnDemandMessage message) throws WriteResponseException {

        UUID accountId = authenticationContextProvider.get().getAccount().getId();

        String productId = message.getProductId();
        LtiConsumerCredential ltiConsumerCredential = message.getLtiConsumerCredential();
        UUID deploymentId = deploymentService.findProductDeploymentId(productId).block();
        UUID newUUID = UUIDs.timeBased();
        UUID cohortId = newUUID;
        JSONObject settings = new JSONObject(message.getSettings());
        DeployedActivity deployedActivity = (deploymentId == null) ?
                null : deploymentService.findLatestDeploymentOrEmpty(message.getActivityId(), deploymentId).block();

        if (deployedActivity != null) {

            cohortId = deployedActivity.getCohortId();

        } else { // first time deploying/publishing this courseware for classes on-demand, create cohort (template)

            // set the class start date to when we create the Bronte cohort template
            final Long startDate = Instant.now().toEpochMilli();
            final Long endDate = null;

            // find the workspace
            UUID newCohortId = cohortId;
            //JSONObject settings = new JSONObject(message.getSettings());
            activityService.findProjectIdByActivity(message.getActivityId())
                    .flatMap(project -> projectService.findWorkspaceIdByProject(project.getProjectId()))
                    .flatMap(workspaceProject -> workspaceService.fetchById(workspaceProject.getWorkspaceId()))
                    // create a cohort using the supplied parameters
                    .flatMap(workspace -> cohortService.createCohort(newCohortId,
                            accountId, workspace.getId(), settings.getString("title")+": On-Demand Cohort Template",
                            EnrollmentType.LTI,
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
                    // try saving the LTI consumer key and secret to this cohort
                    .flatMap(cohortSummary ->
                                     //Use the LTI key/secret from config if its available otherwise use from RTM request.
                        cohortService.saveLTIConsumerKey(cohortSummary, StringUtils.isNotBlank(ltiConfig.getKey()) ? ltiConfig.getKey() : ltiConsumerCredential.getKey(),
                                                         StringUtils.isNotBlank(ltiConfig.getSecret()) ? ltiConfig.getSecret() : ltiConsumerCredential.getSecret())
                    )
                    .doOnEach(log.reactiveInfo("creating new on-demand cohort template with id " + newCohortId))
                    .doOnError(throwable -> {
                        log.reactiveError("error while creating new on-demand cohort template");

                        Responses.errorReactive(session, message.getId(), PUBLICATION_CLASSONDEMAND_PUBLISH_REQUEST_ERROR,
                                HttpStatus.SC_UNPROCESSABLE_ENTITY, "error while creating new on-demand cohort template");

                        throw Exceptions.propagate(throwable);
                    })
                    .block();

        }

        // return OK case at this stage, deploy stage (below) will broadcast deployment success/failure asynchronously
        Responses.writeReactive(session,
                new BasicResponseMessage(PUBLICATION_CLASSONDEMAND_PUBLISH_REQUEST_OK, message.getId())
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
                .flatMap(deployment -> {
                    // publish course metadata
                    log.info("publications settings: {}", settings);

                    String mxLabMetadataUrl = publishMetadataConfig.getMasteringLabMetadataUrl();
                    PublicationSettings publicationSettings = new PublicationSettings()
                            .setLabId("ondemand/product/" + productId)
                            .setTitle(settings.getString("title"))
                            .setDescription(settings.getString("description"))
                            .setDiscipline(settings.getString("discipline"))
                            .setEstimatedTime(settings.getString("estimatedTime"))
                            .setPreviewUrl(settings.getString("previewUrl"))
                            .setStatus(1);

                    log.jsonInfo("publish course metadata", new HashedMap<String, Object>() {
                        {
                            put("mxLabMetadataUrl",  mxLabMetadataUrl);
                            put("publicationSettings",  publicationSettings);
                        }
                    });

                    // todo uncomment after manually testing on-demand publication flow
                    return publishMetadataService.publish(mxLabMetadataUrl, publicationSettings)
                            .thenReturn(deployment);
                    //return Mono.just(deployment);
                })
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscribe(deployment -> {
                    log.jsonInfo("deployed class on-demand activity", new HashedMap<String, Object>() {
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
                    log.error("error publishing on-demand class");

                    // todo rtmEventBroker.broadcast(PROJECT_ACTIVITY_PUBLISH, new PublishedActivityBroadcastMessage(deployment.getCohortId())
                    // todo        .setPublishedActivity(deployment));
                });

    }
}
