package com.smartsparrow.rtm.message.handler.courseware;

import static com.smartsparrow.util.Warrants.affirmArgument;

import javax.inject.Inject;
import javax.inject.Provider;

import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.log.ReactiveMdc;
import org.apache.http.HttpStatus;
import org.eclipse.jetty.websocket.api.Session;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.learner.service.CoursewareElementMetaInformationService;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.MessageHandler;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.event.courseware.ActivityChangeEventPublisher;
import com.smartsparrow.rtm.message.recv.courseware.SetCoursewareElementMetaInformationMessage;
import com.smartsparrow.rtm.message.send.BasicResponseMessage;
import com.smartsparrow.rtm.util.Responses;

import java.util.HashMap;

public class SetCoursewareElementMetaInformationMessageHandler implements MessageHandler<SetCoursewareElementMetaInformationMessage> {

    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(SetCoursewareElementMetaInformationMessageHandler.class);

    public static final String WORKSPACE_COURSEWARE_ELEMENT_INFO_SET = "workspace.courseware.meta.info.set";
    private static final String WORKSPACE_COURSEWARE_ELEMENT_INFO_SET_OK = "workspace.courseware.meta.info.set.ok";
    private static final String WORKSPACE_COURSEWARE_ELEMENT_INFO_SET_ERROR = "workspace.courseware.meta.info.set.error";

    private final CoursewareElementMetaInformationService coursewareElementMetaInformationService;
    private final Provider<RTMEventBroker> rtmEventBrokerProvider;

    @Inject
    public SetCoursewareElementMetaInformationMessageHandler(final CoursewareElementMetaInformationService coursewareElementMetaInformationService,
                                                             final Provider<RTMEventBroker> rtmEventBrokerProvider) {
        this.coursewareElementMetaInformationService = coursewareElementMetaInformationService;
        this.rtmEventBrokerProvider = rtmEventBrokerProvider;
    }

    @Override
    public void validate(final SetCoursewareElementMetaInformationMessage message) throws RTMValidationException {
        affirmArgument(message.getElementId() != null, "elementId is required");
        affirmArgument(message.getElementType() != null, "elementType is required");
        affirmArgument(message.getKey() != null, "key is required");
    }

    @Override
    public void handle(final Session session, final SetCoursewareElementMetaInformationMessage message) throws WriteResponseException {
        final RTMEventBroker rtmEventBroker = rtmEventBrokerProvider.get();
        // create the meta info
        coursewareElementMetaInformationService.createMetaInfo(message.getElementId(), message.getKey(), message.getValue())
                .doOnEach(log.reactiveErrorThrowable("error while creating courseware meta info", throwable -> new HashMap<String, Object>() {
                    {
                        put("elementId", message.getElementId());
                        put("key", message.getKey());
                    }
                }))
                .subscriberContext(ReactiveMdc.with(ReactiveMdc.Property.REQUEST_CONTEXT))
                .subscribe(created -> {
                    // on success, write the created meta info to the client
                    Responses.writeReactive(session, new BasicResponseMessage(WORKSPACE_COURSEWARE_ELEMENT_INFO_SET_OK, message.getId())
                            .addField("coursewareElementMetaInformation", created));
                    // broadcast the message a message for the activity change event publisher
                    rtmEventBroker.broadcast(message.getType(), getData(message));
                }, ex -> {
                    log.jsonDebug("could not create courseware element meta information", new HashMap<String, Object>() {
                        {
                            put("elementId", message.getElementId());
                            put("key", message.getKey());
                            put("error", ex.getStackTrace());
                        }
                    });
                    Responses.errorReactive(session, message.getId(), WORKSPACE_COURSEWARE_ELEMENT_INFO_SET_ERROR,
                            HttpStatus.SC_UNPROCESSABLE_ENTITY, "could not create courseware element meta information");
                });
    }

    /**
     * Creates a courseware element broadcast message with the element information and setting the action to
     * {@link CoursewareAction#COURSEWARE_ELEMENT_META_INFO_CHANGED}.
     * This message can be consumed by the {@link ActivityChangeEventPublisher}
     *
     * @param message the message to get the element information from
     */
    private CoursewareElementBroadcastMessage getData(final SetCoursewareElementMetaInformationMessage message) {
        return new CoursewareElementBroadcastMessage()
                .setElement(CoursewareElement.from(message.getElementId(), message.getElementType()))
                .setAction(CoursewareAction.COURSEWARE_ELEMENT_META_INFO_CHANGED);
    }
}
