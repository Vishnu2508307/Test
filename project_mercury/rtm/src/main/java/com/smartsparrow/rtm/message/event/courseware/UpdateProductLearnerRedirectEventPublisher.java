package com.smartsparrow.rtm.message.event.courseware;

import java.util.UUID;

import javax.inject.Inject;

import com.smartsparrow.cohort.eventmessage.CohortBroadcastMessage;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.redirect.LearnerRedirectType;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.learner.service.LearnerRedirectService;
import com.smartsparrow.rtm.message.event.SimpleEventPublisher;
import com.smartsparrow.rtm.ws.RTMClient;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

/**
 * Responsible for creating a {@link com.smartsparrow.learner.redirect.LearnerRedirect} entry that is redirects a
 * productId url to a deployment in a cohort
 */
public class UpdateProductLearnerRedirectEventPublisher extends SimpleEventPublisher<CohortBroadcastMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(UpdateProductLearnerRedirectEventPublisher.class);

    private final LearnerRedirectService learnerRedirectService;
    private final CohortService cohortService;
    private final DeploymentService deploymentService;

    @Inject
    public UpdateProductLearnerRedirectEventPublisher(final LearnerRedirectService learnerRedirectService,
                                                      final CohortService cohortService,
                                                      final DeploymentService deploymentService) {
        this.learnerRedirectService = learnerRedirectService;
        this.cohortService = cohortService;
        this.deploymentService = deploymentService;
    }

    @Override
    public void publish(RTMClient rtmClient, CohortBroadcastMessage data) {

        final UUID cohortId = data.getCohortId();

        // fetch the cohort settings
        cohortService.fetchCohortSettings(cohortId)
                .doOnEach(log.reactiveInfoSignal("attempting at updating a learner redirect"))
                // filter out when both productId and redirectId are null (there is nothing to do here!)
                .filter(settings -> !(settings.getProductId() == null && settings.getLearnerRedirectId() == null))
                .flatMap(cohortSettings -> {
                    if (cohortSettings.getProductId() == null && cohortSettings.getLearnerRedirectId() != null) {
                        // if a learner redirect exists then delete it! (this only deletes the by_key entry)
                        return learnerRedirectService.delete(cohortSettings.getLearnerRedirectId());
                    }
                    return deploymentService.findDeployments(cohortId)
                            .collectList()
                            // filter out the deployments if the list has 0 or more than 1 deployment
                            .filter(deployments -> deployments.size() == 1)
                            // get the first and only deployment in the list
                            .map(deployments -> deployments.get(0))
                            .flatMap(deployment -> {
                                // check if a redirect exists
                                if (cohortSettings.getLearnerRedirectId() != null) {
                                    // delete the old redirect (this only deletes the by_key entry)
                                    return learnerRedirectService.delete(cohortSettings.getLearnerRedirectId())
                                            // update the existing redirect (generates the updated by_key entry)
                                            .then(learnerRedirectService.update(cohortSettings.getLearnerRedirectId(),
                                                    LearnerRedirectType.PRODUCT, cohortSettings.getProductId(), to(deployment)))
                                            .doOnEach(log.reactiveInfoSignal("learner redirect updated"));
                                }
                                // create otherwise
                                return learnerRedirectService.create(LearnerRedirectType.PRODUCT,
                                        cohortSettings.getProductId(), to(deployment))
                                        // save the redirect id to the setting
                                        .flatMap(learnerRedirect -> cohortService.updateLearnerRedirectId(cohortId, learnerRedirect.getId())
                                                .singleOrEmpty()
                                                .thenReturn(learnerRedirect))
                                        .doOnEach(log.reactiveInfoSignal("learner redirect created"));
                            });
                })
                .subscribe();
    }

    /**
     * Return the redirect destination path including the leading slash
     *
     * @param deployedActivity the deployment to build the path for
     * @return the deployment path
     */
    private String to(DeployedActivity deployedActivity) {
        return String.format("/%s/%s", deployedActivity.getCohortId(), deployedActivity.getId());
    }
}
