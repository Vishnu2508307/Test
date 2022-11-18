package com.smartsparrow.rtm.message.handler.courseware;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.service.CoursewareElementDescriptionService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementDescriptionMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.subscription.courseware.descriptivechange.DescriptiveChangeRTMProducer;
import com.smartsparrow.rtm.util.Responses;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

import reactor.core.publisher.Mono;

public class CoursewareElementDescriptionMessageHandler implements MessageHandler<CoursewareElementDescriptionMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CoursewareElementDescriptionMessageHandler.class);

    public static final String COURSEWARE_DESCRIPTION_SET = "project.courseware.description.set";
    public static final String COURSEWARE_DESCRIPTION_SET_OK = "project.courseware.description.set.ok";
    public static final String COURSEWARE_DESCRIPTION_SET_ERROR = "project.courseware.description.set.error";

    private final CoursewareElementDescriptionService coursewareElementDescriptionService;
    private final CoursewareService coursewareService;
    private final Provider<RTMClientContext> rtmClientContextProvider;
    private final DescriptiveChangeRTMProducer descriptiveChangeRTMProducer;

    @Inject
    public CoursewareElementDescriptionMessageHandler(final CoursewareElementDescriptionService coursewareElementDescriptionService,
                                                      final CoursewareService coursewareService,
                                                      final Provider<RTMClientContext> rtmClientContextProvider,
                                                      final DescriptiveChangeRTMProducer descriptiveChangeRTMProducer) {
        this.coursewareElementDescriptionService = coursewareElementDescriptionService;
        this.coursewareService = coursewareService;
        this.rtmClientContextProvider = rtmClientContextProvider;
        this.descriptiveChangeRTMProducer = descriptiveChangeRTMProducer;
    }

    @Override
    public void validate(final CoursewareElementDescriptionMessage message) throws RTMValidationException {
        try {
            checkArgument(message.getElementId() != null, "elementId is required");
            checkArgument(message.getElementType() != null, "elementType is required");
            checkArgument(message.getDescription() != null, "description is required");
        } catch (IllegalArgumentException e) {
            throw new RTMValidationException(e.getMessage(), message.getId(), COURSEWARE_DESCRIPTION_SET_ERROR);
        }
    }

    @Override
    public void handle(final Session session, final CoursewareElementDescriptionMessage message) throws WriteResponseException {

        RTMClientContext rtmClientContext = rtmClientContextProvider.get();

        //create the courseware element description
        Mono<CoursewareElementDescription> coursewareElementDescriptionMono = coursewareElementDescriptionService.createCoursewareElementDescription(
                message.getElementId(),
                message.getElementType(),
                message.getDescription());
        Mono<UUID> rootElementIdMono = coursewareService.getRootElementId(message.getElementId(),
                                                                          message.getElementType());
        Mono.zip(coursewareElementDescriptionMono, rootElementIdMono).subscribe(tuple2 -> {
            // on success, write the created element description to the client
            Responses.writeReactive(session, new BasicResponseMessage(COURSEWARE_DESCRIPTION_SET_OK, message.getId())
                    .addField("coursewareElementDescription", tuple2.getT1()));
            // broadcast the message for courseware element event publisher
            descriptiveChangeRTMProducer.buildDescriptiveChangeRTMConsumable(rtmClientContext,
                                                                             tuple2.getT2(),
                                                                             message.getElementId(),
                                                                             message.getElementType(),
                                                                             message.getDescription()).produce();
        }, ex -> {
            log.error("could not create courseware element description", ex);
            Responses.errorReactive(session,
                                    message.getId(),
                                    COURSEWARE_DESCRIPTION_SET_ERROR,
                                    HttpStatus.SC_UNPROCESSABLE_ENTITY,
                                    "could not create courseware element description");
        });
    }

}
