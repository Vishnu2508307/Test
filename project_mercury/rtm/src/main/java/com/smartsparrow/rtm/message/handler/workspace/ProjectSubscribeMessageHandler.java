package com.smartsparrow.rtm.message.handler.workspace;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.eclipse.jetty.websocket.api.Session;

import com.google.inject.Provider;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.workspace.ProjectGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.data.RTMSubscriptionManager;
import com.smartsparrow.rtm.subscription.project.ProjectEventRTMSubscription;
import com.smartsparrow.rtm.subscription.project.ProjectEventRTMSubscription.ProjectEventRTMSubscriptionFactory;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class ProjectSubscribeMessageHandler implements MessageHandler<ProjectGenericMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ProjectSubscribeMessageHandler.class);

    public static final String WORKSPACE_PROJECT_SUBSCRIBE = "workspace.project.subscribe";
    private static final String WORKSPACE_PROJECT_SUBSCRIBE_OK = "workspace.project.subscribe.ok";

    private final Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider;
    private final ProjectEventRTMSubscriptionFactory projectEventRTMSubscriptionFactory;

    @Inject
    public ProjectSubscribeMessageHandler(final Provider<RTMSubscriptionManager> rtmSubscriptionManagerProvider,
                                          final ProjectEventRTMSubscriptionFactory projectEventRTMSubscriptionFactory) {
        this.rtmSubscriptionManagerProvider = rtmSubscriptionManagerProvider;
        this.projectEventRTMSubscriptionFactory = projectEventRTMSubscriptionFactory;
    }

    @Override
    public void validate(ProjectGenericMessage message) {
        affirmArgument(message.getProjectId() != null, "projectId is required");
    }

    @Override
    public void handle(Session session, ProjectGenericMessage message) throws WriteResponseException {

        ProjectEventRTMSubscription projectEventRTMSubscription = projectEventRTMSubscriptionFactory.create(message.getProjectId());

        rtmSubscriptionManagerProvider.get().add(projectEventRTMSubscription)
                .subscribe(listenerId -> {},
                           this::subscriptionOnErrorHandler,
                           () -> {
                                log.jsonDebug("client subscribing to events ", new HashMap<>() {
                                    {
                                        put("projectId", message.getProjectId());
                                    }
                                });
                               Responses.writeReactive(session, new BasicResponseMessage(WORKSPACE_PROJECT_SUBSCRIBE_OK,
                                                                                         message.getId())
                                       .addField("rtmSubscriptionId", projectEventRTMSubscription.getId()));
                });

    }

}
