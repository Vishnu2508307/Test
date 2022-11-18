package com.smartsparrow.rtm.message.handler.courseware;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.HashMap;

import javax.inject.Inject;

import com.smartsparrow.rtm.util.NewRelic;
import com.smartsparrow.rtm.util.NewRelicTransactionAttributes;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.FindCoursewareProjectMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class FindCoursewareProjectSummaryMessageHandler implements MessageHandler<FindCoursewareProjectMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(FindCoursewareProjectSummaryMessageHandler.class);

    public static final String AUTHOR_COURSEWARE_PROJECT_FIND = "author.courseware.project.find";
    public static final String AUTHOR_COURSEWARE_PROJECT_FIND_OK = "author.courseware.project.find.ok";
    public static final String AUTHOR_COURSEWARE_PROJECT_FIND_ERROR = "author.courseware.project.find.error";

    private final CoursewareService coursewareService;

    @Inject
    public FindCoursewareProjectSummaryMessageHandler(final CoursewareService coursewareService) {
        this.coursewareService = coursewareService;
    }

    @Override
    public void validate(FindCoursewareProjectMessage message) throws RTMValidationException {
        affirmArgument(message.getElementId() != null, "missing elementId");
        affirmArgument(message.getElementType() != null, "missing elementType");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = AUTHOR_COURSEWARE_PROJECT_FIND)
    @Override
    public void handle(Session session, FindCoursewareProjectMessage message) throws WriteResponseException {
        NewRelic.addCustomAttribute(NewRelicTransactionAttributes.ELEMENT_ID.getValue(), message.getElementId().toString(), log);

        coursewareService.findProjectSummary(message.getElementId(),
                                             message.getElementType())
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(ReactiveTransaction.expireOnComplete())
                .subscriberContext(ReactiveMonitoring.createContext())
                .doOnEach(log.reactiveErrorThrowable("error finding courseware project summary",
                                                     throwable -> new HashMap<String, Object>() {
                                                         {
                                                             put("elementId", message.getElementId());
                                                         }
                                                     }))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscribe(projectSummary -> {
                               BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                                       AUTHOR_COURSEWARE_PROJECT_FIND_OK,
                                       message.getId());
                               basicResponseMessage.addField("projectSummary", projectSummary);
                               Responses.writeReactive(session, basicResponseMessage);
                           },
                           ex -> {
                               log.debug("Unable to find courseware project summary", new HashMap<String, Object>() {
                                   {
                                       put("message", message.toString());
                                       put("error", ex.getStackTrace());
                                   }
                               });
                               Responses.errorReactive(session,
                                                       message.getId(),
                                                       AUTHOR_COURSEWARE_PROJECT_FIND_ERROR,
                                                       HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                                       "Unable to find courseware project summary");
                           }
                );

    }
}
