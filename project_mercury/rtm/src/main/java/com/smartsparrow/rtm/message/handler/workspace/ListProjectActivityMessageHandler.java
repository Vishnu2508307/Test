package com.smartsparrow.rtm.message.handler.workspace;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.courseware.service.ActivitySummaryService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.workspace.ProjectActivityListMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class ListProjectActivityMessageHandler implements MessageHandler<ProjectActivityListMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ListProjectActivityMessageHandler.class);

    public static final String PROJECT_ACTIVITY_LIST = "project.activity.list";
    private static final String PROJECT_ACTIVITY_LIST_OK = "project.activity.list.ok";
    private static final String PROJECT_ACTIVITY_LIST_ERROR = "project.activity.list.error";


    private final ActivitySummaryService activitySummaryService;

    @Inject
    public ListProjectActivityMessageHandler(ActivitySummaryService activitySummaryService) {
        this.activitySummaryService = activitySummaryService;
    }

    @Override
    public void validate(final ProjectActivityListMessage message) throws RTMValidationException {
        affirmArgument(message.getProjectId() != null, "projectId is required");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = PROJECT_ACTIVITY_LIST)
    @Override
    public void handle(final Session session, final ProjectActivityListMessage message) throws WriteResponseException {
        List<String> fieldNames = message.getFieldNames() != null ? message.getFieldNames() : Collections.emptyList();
        activitySummaryService.findActivitiesSummaryForProject(message.getProjectId(),fieldNames)
                .doOnEach(log.reactiveErrorThrowable("error while fetching activities", throwable -> new HashMap<String, Object>() {
                    {
                        put("projectId", message.getProjectId());
                        put("fieldNames",fieldNames);
                    }
                }))
                // link each signal to the current transaction token
                .doOnEach(ReactiveTransaction.linkOnNext())
                // expire the transaction token on completion
                .doOnEach(ReactiveTransaction.expireOnComplete())
                // create a reactive context that enables all supported reactive monitoring
                .subscriberContext(ReactiveMonitoring.createContext())
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .collectList()
                .subscribe(activities -> {
                    Responses.writeReactive(session, new BasicResponseMessage(PROJECT_ACTIVITY_LIST_OK, message.getId())
                            .addField("activities", activities));
                }, ex -> {
                    log.jsonDebug("could not list activities for project", new HashMap<String, Object>() {
                        {
                            put("projectId", message.getProjectId());
                            put("fieldNames", message.getFieldNames());
                            put("error", ex.getStackTrace());
                        }
                    });

                    Responses.errorReactive(session, message.getId(), PROJECT_ACTIVITY_LIST_ERROR,
                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "error fetching activities");
                });
    }
}
