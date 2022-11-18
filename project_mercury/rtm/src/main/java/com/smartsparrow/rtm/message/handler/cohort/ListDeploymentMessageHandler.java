package com.smartsparrow.rtm.message.handler.cohort;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;

import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.learner.payload.DeploymentPayload;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.learner.service.LearnerActivityService;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.cohort.ListDeploymentMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;

public class ListDeploymentMessageHandler implements MessageHandler<ListDeploymentMessage> {

    public static final String WORKSPACE_DEPLOYMENT_LIST = "workspace.deployment.list";
    public static final String WORKSPACE_DEPLOYMENT_LIST_OK = "workspace.deployment.list.ok";
    public static final String WORKSPACE_DEPLOYMENT_LIST_ERROR = "workspace.deployment.list.error";

    private final DeploymentService deploymentService;
    private final LearnerActivityService learnerActivityService;
    private final PluginService pluginService;

    @Inject
    public ListDeploymentMessageHandler(DeploymentService deploymentService,
                                        LearnerActivityService learnerActivityService,
                                        PluginService pluginService) {
        this.deploymentService = deploymentService;
        this.learnerActivityService = learnerActivityService;
        this.pluginService = pluginService;
    }

    @Override
    public void validate(ListDeploymentMessage message) throws RTMValidationException {
        affirmArgument(message.getCohortId() != null, "cohortId is required");
    }

    @Override
    public void handle(Session session, ListDeploymentMessage message) {
        deploymentService.findDeployments(message.getCohortId())
                .flatMap(deployment -> learnerActivityService.findActivity(deployment.getActivityId(),
                                                                           deployment.getId()))
                .flatMap(activity -> pluginService.fetchById(activity.getPluginId()).map(p -> pluginService.fetchPluginFiltersByIdVersionExpr(
                        activity.getPluginId(),
                        activity.getPluginVersionExpr())
                        .map(pluginFilters -> DeploymentPayload.from(activity, p, pluginFilters))
                ).flatMap(deploymnetPayload -> {
                    return deploymnetPayload;
                }))
                .collectList()
                .subscribe(list -> {
                    BasicResponseMessage basicResponseMessage =
                            new BasicResponseMessage(WORKSPACE_DEPLOYMENT_LIST_OK, message.getId()).addField(
                                    "deployments",
                                    list);
                    Responses.writeReactive(session, basicResponseMessage);
                }, ex -> Responses.errorReactive(session, message.getId(), WORKSPACE_DEPLOYMENT_LIST_ERROR, ex));
    }
}
