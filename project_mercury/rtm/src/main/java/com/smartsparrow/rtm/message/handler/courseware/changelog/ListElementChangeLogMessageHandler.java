package com.smartsparrow.rtm.message.handler.courseware.changelog;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.courseware.service.CoursewareChangeLogService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.workspace.ElementChangeLogListMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

public class ListElementChangeLogMessageHandler implements MessageHandler<ElementChangeLogListMessage> {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ListElementChangeLogMessageHandler.class);

    private final CoursewareChangeLogService coursewareChangeLogService;

    public static final String ELEMENT_CHANGELOG_LIST = "project.courseware.changelog.list";
    private static final String ELEMENT_CHANGELOG_LIST_OK = "project.courseware.changelog.list.ok";
    private static final String ELEMENT_CHANGELOG_LIST_ERROR = "project.courseware.changelog.list.error";
    private static final Integer LIMIT = 50;
    private static final Integer MAX_LIMIT = 100;

    @Inject
    public ListElementChangeLogMessageHandler(CoursewareChangeLogService coursewareChangeLogService) {
        this.coursewareChangeLogService = coursewareChangeLogService;
    }

    @Override
    public void validate(ElementChangeLogListMessage message) throws RTMValidationException {
        affirmArgument(message.getElementId() != null, "elementId is required");
    }

    @Override
    public void handle(Session session, ElementChangeLogListMessage message) throws WriteResponseException {
        coursewareChangeLogService
                .fetchCoursewareChangeLogByElement(message.getElementId(),computeLimit(message.getLimit()))
                .collectList()
                .subscribe(changelogs -> {
                    Responses.writeReactive(session, new BasicResponseMessage(ELEMENT_CHANGELOG_LIST_OK, message.getId())
                            .addField("changelogs", changelogs));
                }, ex -> {
                    log.jsonDebug("could not list change logs for element", new HashMap<String, Object>() {
                        {
                            put("elementId", message.getElementId());
                            put("error", ex.getStackTrace());
                        }
                    });

                    Responses.errorReactive(session, message.getId(), ELEMENT_CHANGELOG_LIST_ERROR,
                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "error fetching courseware element change logs");
                });
    }

    private Integer computeLimit(Integer requestLimit){
        if(requestLimit != null && requestLimit < MAX_LIMIT){
            return requestLimit;
        }
        return LIMIT;
    }
}
