package com.smartsparrow.rtm.message.handler.courseware.publication;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.publication.service.PublicationService;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.publication.PublicationActivityFetchMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.workspace.service.ProjectService;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import javax.inject.Inject;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PublicationActivityFetchMessageHandler implements MessageHandler<PublicationActivityFetchMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(PublicationActivityFetchMessageHandler.class);

    public static final String PUBLICATION_ACTIVITY_FETCH = "publication.activity.fetch";
    public static final String PUBLICATION_ACTIVITY_FETCH_OK = "publication.activity.fetch.ok";
    public static final String PUBLICATION_ACTIVITY_FETCH_ERROR = "publication.activity.fetch.error";

    private final PublicationService publicationService;
    private final ProjectService projectService;

    @Inject
    public PublicationActivityFetchMessageHandler(final PublicationService publicationService,
                                                  final ProjectService projectService) {
        this.publicationService = publicationService;
        this.projectService = projectService;
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = PUBLICATION_ACTIVITY_FETCH)
    @Override
    public void handle(Session session, PublicationActivityFetchMessage message) throws WriteResponseException {

        try {
            if (message.getWorkspaceId() != null) {
                List<UUID> activityList = projectService.findWorkspaceActivities(message.getWorkspaceId())
                        .doOnEach(log.reactiveErrorThrowable("error fetching the activities",
                                throwable -> new HashMap<String, Object>() {
                                    {
                                        put("workspaceId", message.getWorkspaceId());
                                    }
                                }))
                        .collectList()
                        .block();

                publicationService.fetchPublishedActivities()
                        .filter(payload -> activityList.contains(payload.getActivityId()))
                        .collectList()
                        .subscribe(activityPayloadList -> {
                            BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                    PUBLICATION_ACTIVITY_FETCH_OK,
                                    message.getId());
                            basicResponseMessage.addField("activities", activityPayloadList);
                            Responses.writeReactive(session, basicResponseMessage);
                        });
            } else {
                publicationService.fetchPublishedActivities()
                        .collectList()
                        .subscribe(activityPayloadList -> {
                            BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                    PUBLICATION_ACTIVITY_FETCH_OK,
                                    message.getId());
                            basicResponseMessage.addField("activities", activityPayloadList);
                            Responses.writeReactive(session, basicResponseMessage);
                        });
            }
        }

        catch (Exception ex) {
            Responses.errorReactive(session, message.getId(), PUBLICATION_ACTIVITY_FETCH_ERROR,
                    HttpStatus.SC_UNPROCESSABLE_ENTITY, "error fetch activities");
        }
    }
}
