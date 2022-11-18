package com.smartsparrow.rtm.message.handler.workspace;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;
import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.workspace.ProjectGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.SubscriptionManager;
import com.smartsparrow.rtm.subscription.project.ProjectChangeLogSubscription;
import com.smartsparrow.rtm.util.Responses;

import reactor.core.publisher.Mono;

public class ProjectChangeLogSubscribeMessageHandler implements MessageHandler<ProjectGenericMessage> {

    public static final String PROJECT_CHANGELOG_SUBSCRIBE = "project.changelog.subscribe";
    private static final String PROJECT_CHANGELOG_SUBSCRIBE_OK = "project.changelog.subscribe.ok";
    private static final String PROJECT_CHANGELOG_SUBSCRIBE_ERROR = "project.changelog.subscribe.error";
    public static final String PROJECT_CHANGELOG_SUBSCRIBE_BROADCAST = "project.changelog.broadcast";
    public static final String PROJECT_CHANGELOG_SUBSCRIBE_BROADCAST_ERROR = "project.changelog.broadcast.error";

    private final Provider<SubscriptionManager> subscriptionManagerProvider;
    private final ProjectChangeLogSubscription projectChangeLogSubscription;

    @Inject
    public ProjectChangeLogSubscribeMessageHandler(final Provider<SubscriptionManager> subscriptionManagerProvider,
                                                   final ProjectChangeLogSubscription projectChangeLogSubscription) {
        this.subscriptionManagerProvider = subscriptionManagerProvider;
        this.projectChangeLogSubscription = projectChangeLogSubscription;
    }

    @Override
    public void validate(ProjectGenericMessage message) throws RTMValidationException {
        affirmArgument(message.getProjectId() != null, "projectId is required");

        // TODO check that this is a top level activity can only subscribe to those
    }

    @SuppressWarnings({"unchecked", "Duplicates"})
    @Override
    public void handle(Session session, ProjectGenericMessage message) throws WriteResponseException {
        final SubscriptionManager subscriptionManager = subscriptionManagerProvider.get();

        projectChangeLogSubscription.setProjectId(message.getProjectId());

        Mono<Integer> add = subscriptionManager.add(projectChangeLogSubscription);

        add.subscribe(listenerId -> {
           // nothing to do here
        }, this::subscriptionOnErrorHandler, () -> {
            BasicResponseMessage response = new BasicResponseMessage(PROJECT_CHANGELOG_SUBSCRIBE_OK, message.getId());
            response.addField("rtmSubscriptionId", projectChangeLogSubscription.getId());
            Responses.writeReactive(session, response);
        });
    }
}
