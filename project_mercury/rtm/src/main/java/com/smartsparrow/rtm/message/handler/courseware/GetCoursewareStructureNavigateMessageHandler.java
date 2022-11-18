package com.smartsparrow.rtm.message.handler.courseware;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.service.CoursewareElementStructureNavigateService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.GetCoursewareElementStructureNavigateMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.NewRelic;
import com.smartsparrow.rtm.util.NewRelicTransactionAttributes;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.smartsparrow.util.Warrants.affirmArgument;

public class GetCoursewareStructureNavigateMessageHandler implements MessageHandler<GetCoursewareElementStructureNavigateMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GetCoursewareStructureNavigateMessageHandler.class);

    public static final String COURSEWARE_STRUCTURE_NAVIGATE = "author.courseware.structure.navigate";
    public static final String COURSEWARE_STRUCTURE_NAVIGATE_OK = "author.courseware.structure.navigate.ok";
    public static final String COURSEWARE_STRUCTURE_NAVIGATE_ERROR = "author.courseware.structure.navigate.error";

    private final CoursewareElementStructureNavigateService coursewareElementStructureNavigateService;

    @Inject
    public GetCoursewareStructureNavigateMessageHandler(final CoursewareElementStructureNavigateService coursewareElementStructureNavigateService) {
        this.coursewareElementStructureNavigateService = coursewareElementStructureNavigateService;
    }

    @Override
    public void validate(final GetCoursewareElementStructureNavigateMessage message) throws RTMValidationException {
        affirmArgument(message.getElementId() != null, "elementId is required");
        affirmArgument(message.getElementType() != null, "elementType is required");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = COURSEWARE_STRUCTURE_NAVIGATE)
    @Override
    public void handle(final Session session, final GetCoursewareElementStructureNavigateMessage message) throws WriteResponseException {
        List<String> fieldNames = message.getFieldNames() != null ? message.getFieldNames() : Collections.emptyList();
        NewRelic.addCustomAttribute(NewRelicTransactionAttributes.ELEMENT_ID.getValue(), message.getElementId().toString(), log);

        coursewareElementStructureNavigateService.getCoursewareElementStructure(message.getElementId(), message.getElementType(), fieldNames)
            .doOnEach(log.reactiveErrorThrowable("error fetching courseware element navigate structure", throwable -> new HashMap<String, Object>() {
                {
                    put("elementId", message.getElementId());
                }
            }))
            // link each signal to the current transaction token
            .doOnEach(ReactiveTransaction.linkOnNext())
            // expire the transaction token on completion
            .doOnEach(ReactiveTransaction.expireOnComplete())
            // create a reactive context that enables all supported reactive monitoring
            .subscriberContext(ReactiveMonitoring.createContext())
            .subscribe(coursewareStructure -> {
                   BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                           COURSEWARE_STRUCTURE_NAVIGATE_OK,
                           message.getId());
                   basicResponseMessage.addField("coursewareStructure", coursewareStructure);
                   Responses.writeReactive(session, basicResponseMessage);
               }, ex -> {
                   log.jsonDebug("Unable to get element structure", new HashMap<String, Object>(){
                       {
                           put("message", message.toString());
                           put("error", ex.getStackTrace());
                       }
                   });
                   Responses.errorReactive(session, message.getId(), COURSEWARE_STRUCTURE_NAVIGATE_ERROR,
                                           HttpStatus.SC_UNPROCESSABLE_ENTITY, "error fetching courseware element navigate structure");
               });
    }
}
