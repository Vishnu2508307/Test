package com.smartsparrow.rtm.message.handler.courseware;

import static com.smartsparrow.util.Warrants.affirmArgument;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

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

public class GetCoursewareSupportStructureMessageHandler implements MessageHandler<GetCoursewareElementStructureMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GetCoursewareSupportStructureMessageHandler.class);

    public static final String SUPPORT_COURSEWARE_STRUCTURE_GET = "author.courseware.support.structure.get";
    public static final String SUPPORT_COURSEWARE_STRUCTURE_GET_OK = "author.courseware.support.structure.get.ok";
    public static final String SUPPORT_COURSEWARE_STRUCTURE_GET_ERROR = "author.courseware.support.structure.get.error";

    private final CoursewareElementStructureService coursewareElementStructureService;

    @Inject
    public GetCoursewareSupportStructureMessageHandler(final CoursewareElementStructureService coursewareElementStructureService) {
        this.coursewareElementStructureService = coursewareElementStructureService;
    }

    @Override
    public void validate(final GetCoursewareElementStructureMessage message) throws RTMValidationException {
        affirmArgument(message.getElementId() != null, "elementId is required");
        affirmArgument(message.getElementType() != null, "elementType is required");
    }

    @Override
    public void handle(final Session session, final GetCoursewareElementStructureMessage message) throws WriteResponseException {
        coursewareElementStructureService.getCoursewareElementStructure(message.getElementId(), message.getElementType(), Collections.emptyList())
            .doOnEach(log.reactiveErrorThrowable("error fetching support courseware element structure", throwable -> new HashMap<String, Object>() {
                {
                    put("elementId", message.getElementId());
                }
            }))
            .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
            .subscribe(coursewareStructure -> {
                   BasicResponseMessage basicResponseMessage = new BasicResponseMessage(
                           SUPPORT_COURSEWARE_STRUCTURE_GET_OK,
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
                   Responses.errorReactive(session, message.getId(), SUPPORT_COURSEWARE_STRUCTURE_GET_ERROR,
                                           HttpStatus.SC_UNPROCESSABLE_ENTITY, "error fetching support courseware element structure");
               });
    }
}
