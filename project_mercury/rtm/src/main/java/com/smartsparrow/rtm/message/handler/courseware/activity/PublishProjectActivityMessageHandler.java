package com.smartsparrow.rtm.message.handler.courseware.activity;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.cohort.data.CohortSettings;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.courseware.data.ActivityChange;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.PublishedActivityBroadcastMessage;
import com.smartsparrow.courseware.lang.ActivityChangeNotFoundException;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.lang.DeploymentNotFoundException;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.activity.PublishActivityMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import reactor.core.publisher.Mono;

public class PublishProjectActivityMessageHandler implements MessageHandler<PublishActivityMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(PublishProjectActivityMessageHandler.class);

    public static final String PROJECT_ACTIVITY_PUBLISH = "project.activity.publish";
    private static final String PROJECT_ACTIVITY_PUBLISH_OK = "project.activity.publish.ok";
    private static final String PROJECT_ACTIVITY_PUBLISH_ERROR = "project.activity.publish.error";

    private final CoursewareService coursewareService;
    private final DeploymentService deploymentService;
    private final CohortService cohortService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;

    @Inject
    public PublishProjectActivityMessageHandler(final CoursewareService coursewareService,
                                                final DeploymentService deploymentService,
                                                final CohortService cohortService,
                                                final Provider<RTMEventBroker> rtmEventBrokerProvider) {
        this.coursewareService = coursewareService;
        this.deploymentService = deploymentService;
        this.cohortService = cohortService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
    }

    /**
     * Check the validity of a publish activity message. The method checks on the following rules:
     * <br> - the activityId must be supplied and be of a top level activity (with no parents)
     * <br> When the deployment id is supplied:
     * <br> - the deployment must exists already
     * <br> - recent changes in the activity must be detected against the existing deployment
     * <br> - the cohortId field is supplied
     * <br> - the cohort exists
     * @param message the received message that requires to be validated
     * @throws RTMValidationException when the message is invalid
     */
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
    justification = "all dereference values are checked for null value before computation")
    @Override
    public void validate(PublishActivityMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getActivityId() != null, "activityId is required");
            checkArgument(message.getCohortId() != null, "cohortId is required");

            checkArgument(cohortService.fetchCohortSummary(message.getCohortId()).block() != null,
                    String.format("cohort %s not found", message.getCohortId()));

            List<UUID> parentActivityIds = coursewareService
                    .getParentActivityIds(message.getActivityId(), CoursewareElementType.ACTIVITY).block();

            checkArgument(parentActivityIds != null, "could not verify activity");
            checkArgument(!parentActivityIds.isEmpty(), "could not verify activity");
            checkArgument(message.getActivityId().equals(parentActivityIds.get(0)),
                    String.format("%s is not a top level activity", message.getActivityId()));

            if (message.getDeploymentId() != null) {
                DeployedActivity deployment = deploymentService
                        .findLatestDeployment(message.getActivityId(), message.getDeploymentId()).block();

                checkArgument(deployment != null, "deployment not found");

                ActivityChange latestChange = coursewareService.findLatestChange(message.getActivityId()).block();

                checkArgument(latestChange != null, "activity change is required");
                checkArgument(deployment.getChangeId() != null, "change id on deployment is required");
                checkArgument(!deployment.getChangeId().equals(latestChange.getChangeId()),
                        "no changes detected, upload action not required");
            }
        } catch (IllegalArgumentException | DeploymentNotFoundException | ActivityChangeNotFoundException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), PROJECT_ACTIVITY_PUBLISH_ERROR);
        }
    }

    @Override
    public void handle(Session session, PublishActivityMessage message) throws WriteResponseException {
        final RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        deploymentService.deploy(message.getActivityId(),
                                 message.getCohortId(),
                                 message.getDeploymentId(),
                                 message.isLockPluginVersionEnabled())
                .doOnEach(log.reactiveErrorThrowable("error publishing activity ", throwable -> new HashMap<String, Object>() {
                    {
                        put("activityId", message.getActivityId());
                        put("deploymentId", message.getDeploymentId());
                        put("cohortId", message.getCohortId());
                    }
                }))
                .flatMap(deployment -> {
                    // create/update association of product id to deployment id
                    return cohortService.fetchCohortSettings(message.getCohortId())
                            .defaultIfEmpty(new CohortSettings())
                            .flatMap(cohortSettings -> {
                                if (cohortSettings.getProductId() != null) {
                                    return deploymentService.saveProductDeploymentId(cohortSettings.getProductId(), deployment.getId())
                                            .thenReturn(deployment);
                                } else {
                                    log.warn("no product id associated in settings for cohort id: ", message.getCohortId());
                                    return Mono.just(deployment);
                                }
                            });
                })
                .doOnEach(log.reactiveErrorThrowable("error fetching or saving product id", throwable -> new HashMap<String, Object>() {
                    {
                        put("activityId", message.getActivityId());
                        put("deploymentId", message.getDeploymentId());
                        put("cohortId", message.getCohortId());
                    }
                }))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscribe(deployment -> {
                    Responses.writeReactive(session, new BasicResponseMessage(PROJECT_ACTIVITY_PUBLISH_OK, message.getId())
                    .addField("deployment", deployment));
                    rtmEventBroker.broadcast(PROJECT_ACTIVITY_PUBLISH, new PublishedActivityBroadcastMessage(deployment.getCohortId())
                            .setPublishedActivity(deployment));
                }, ex -> {
                    Responses.errorReactive(session, message.getId(), PROJECT_ACTIVITY_PUBLISH_ERROR,
                            HttpStatus.SC_UNPROCESSABLE_ENTITY,
                            "an error occurred while publishing the activity");
                });
    }
}
