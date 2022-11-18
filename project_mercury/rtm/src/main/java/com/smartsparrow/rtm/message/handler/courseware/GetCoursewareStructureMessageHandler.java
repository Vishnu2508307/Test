package com.smartsparrow.rtm.message.handler.courseware;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.service.CoursewareElementStructureService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.GetCoursewareElementStructureMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import com.smartsparrow.util.monitoring.ReactiveMonitoring;
import com.smartsparrow.util.monitoring.ReactiveTransaction;

public class GetCoursewareStructureMessageHandler implements MessageHandler<GetCoursewareElementStructureMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GetCoursewareStructureMessageHandler.class);

    public static final String COURSEWARE_STRUCTURE_GET = "author.courseware.structure.get";
    public static final String COURSEWARE_STRUCTURE_GET_OK = "author.courseware.structure.get.ok";
    public static final String COURSEWARE_STRUCTURE_GET_ERROR = "author.courseware.structure.get.error";

    private final CoursewareElementStructureService coursewareElementStructureService;

    @Inject
    public GetCoursewareStructureMessageHandler(final CoursewareElementStructureService coursewareElementStructureService) {
        this.coursewareElementStructureService = coursewareElementStructureService;
    }

    @Override
    public void validate(final GetCoursewareElementStructureMessage message) throws RTMValidationException {
        affirmArgument(message.getElementId() != null, "elementId is required");
        affirmArgument(message.getElementType() != null, "elementType is required");
    }

    @Trace(dispatcher = true, nameTransaction = false, metricName = COURSEWARE_STRUCTURE_GET)
    @Override
    public void handle(final Session session, final GetCoursewareElementStructureMessage message) throws WriteResponseException {
        List<String> fieldNames = message.getFieldNames() != null ? message.getFieldNames() : Collections.emptyList();

        coursewareElementStructureService.getCoursewareElementStructure(message.getElementId(), message.getElementType(), fieldNames)
            .doOnEach(log.reactiveErrorThrowable("error fetching courseware element structure", throwable -> new HashMap<String, Object>() {
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
            .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
            .subscribe(coursewareStructure -> {
                   BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                           COURSEWARE_STRUCTURE_GET_OK,
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
                   Responses.errorReactive(session, message.getId(), COURSEWARE_STRUCTURE_GET_ERROR,
                                           HttpStatus.SC_UNPROCESSABLE_ENTITY, "error fetching courseware element structure");
               });
    }
}
