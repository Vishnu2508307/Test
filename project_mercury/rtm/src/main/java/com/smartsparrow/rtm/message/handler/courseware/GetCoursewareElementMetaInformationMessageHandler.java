package com.smartsparrow.rtm.message.handler.courseware;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;

import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.learner.service.CoursewareElementMetaInformationService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.GetCoursewareElementMetaInformationMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;

import java.util.HashMap;

public class GetCoursewareElementMetaInformationMessageHandler implements MessageHandler<GetCoursewareElementMetaInformationMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(GetCoursewareElementMetaInformationMessageHandler.class);

    public static final String WORKSPACE_COURSEWARE_ELEMENT_INFO_GET = "workspace.courseware.meta.info.get";
    private static final String WORKSPACE_COURSEWARE_ELEMENT_INFO_GET_OK = "workspace.courseware.meta.info.get.ok";
    private static final String WORKSPACE_COURSEWARE_ELEMENT_INFO_GET_ERROR = "workspace.courseware.meta.info.get.error";

    private final CoursewareElementMetaInformationService coursewareElementMetaInformationService;

    @Inject
    public GetCoursewareElementMetaInformationMessageHandler(final CoursewareElementMetaInformationService coursewareElementMetaInformationService) {
        this.coursewareElementMetaInformationService = coursewareElementMetaInformationService;
    }

    @Override
    public void validate(final GetCoursewareElementMetaInformationMessage message) throws RTMValidationException {
        affirmArgument(message.getElementId() != null, "elementId is required");
        affirmArgument(message.getElementType() != null, "elementType is required");
        affirmArgument(message.getKey() != null, "key is required");
    }

    @Override
    public void handle(final Session session, final GetCoursewareElementMetaInformationMessage message) throws WriteResponseException {
        coursewareElementMetaInformationService.findMetaInfo(message.getElementId(), message.getKey())
                .doOnEach(log.reactiveErrorThrowable("error fetching courseware element meta info", throwable -> new HashMap<String, Object>() {
                    {
                        put("elementId", message.getElementId());
                        put("key", message.getKey());
                    }
                }))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscribe(metaInfo -> {
                    Responses.writeReactive(session, new BasicResponseMessage(WORKSPACE_COURSEWARE_ELEMENT_INFO_GET_OK,
                            message.getId())
                            .addField("coursewareElementMetaInformation", metaInfo));
                }, ex -> {
                    log.jsonDebug("could not fetch courseware element meta information", new HashMap<String, Object>() {
                        {
                            put("elementId", message.getElementId());
                            put("key", message.getKey());
                            put("error", ex.getStackTrace());
                        }
                    });
                    Responses.errorReactive(session, message.getId(), WORKSPACE_COURSEWARE_ELEMENT_INFO_GET_ERROR,
                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "could not fetch courseware element meta information");
                });
    }
}
