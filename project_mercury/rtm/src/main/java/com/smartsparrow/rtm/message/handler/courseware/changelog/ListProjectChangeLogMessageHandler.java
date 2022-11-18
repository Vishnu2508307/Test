package com.smartsparrow.rtm.message.handler.courseware.changelog;

import com.smartsparrow.courseware.service.CoursewareChangeLogService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.workspace.ProjectChangeLogListMessage;
import com.smartsparrow.rtm.message.recv.workspace.ProjectGenericMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import javax.inject.Inject;

import java.util.HashMap;

import static com.smartsparrow.util.Warrants.affirmArgument;

public class ListProjectChangeLogMessageHandler implements MessageHandler<ProjectChangeLogListMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ListProjectChangeLogMessageHandler.class);

    public static final String PROJECT_CHANGELOG_LIST = "project.changelog.list";
    private static final String PROJECT_CHANGELOG_LIST_OK = "project.changelog.list.ok";
    private static final String PROJECT_CHANGELOG_LIST_ERROR = "project.changelog.list.error";
    private static final Integer LIMIT = 50;


    private final CoursewareChangeLogService coursewareChangeLogService;

    @Inject
    public ListProjectChangeLogMessageHandler(final CoursewareChangeLogService coursewareChangeLogService) {
        this.coursewareChangeLogService = coursewareChangeLogService;
    }

    @Override
    public void validate(final ProjectChangeLogListMessage message) throws RTMValidationException {
        affirmArgument(message.getProjectId() != null, "projectId is required");
    }

    @Override
    public void handle(Session session, ProjectChangeLogListMessage message) throws WriteResponseException {
        coursewareChangeLogService.fetchCoursewareChangeLogByProject(message.getProjectId(),  message.getLimit() != null ? message.getLimit() : LIMIT)
                .doOnEach(log.reactiveErrorThrowable("error while fetching changelogs", throwable -> new HashMap<String, Object>() {
                    {
                        put("projectId", message.getProjectId());
                    }
                }))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .collectList()
                .subscribe(changelogs -> {
                    Responses.writeReactive(session, new BasicResponseMessage(PROJECT_CHANGELOG_LIST_OK, message.getId())
                            .addField("projectchangelogs", changelogs));
                }, ex -> {
                    log.jsonDebug("could not list change logs for project", new HashMap<String, Object>() {
                        {
                            put("projectId", message.getProjectId());
                            put("error", ex.getStackTrace());
                        }
                    });

                    Responses.errorReactive(session, message.getId(), PROJECT_CHANGELOG_LIST_ERROR,
                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "error fetching courseware project change logs");
                });
    }
}
