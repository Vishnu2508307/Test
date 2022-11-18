package com.smartsparrow.rtm.message.handler.courseware;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.courseware.CoursewareElementDescriptionMessageHandler.COURSEWARE_DESCRIPTION_SET_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareElementDescriptionService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.recv.courseware.CoursewareElementDescriptionMessage;
import com.smartsparrow.rtm.subscription.courseware.descriptivechange.DescriptiveChangeRTMProducer;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class CoursewareElementDescriptionMessageHandlerTest {

    private static final UUID elementId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final CoursewareElementType elementType = ACTIVITY;
    private static final String description = "test";
    @InjectMocks
    private CoursewareElementDescriptionMessageHandler handler;
    @Mock
    private CoursewareElementDescriptionService coursewareElementDescriptionService;
    @Mock
    private CoursewareService coursewareService;
    @Mock
    private CoursewareElementDescriptionMessage message;
    @Mock
    private DescriptiveChangeRTMProducer descriptiveChangeRTMProducer;
    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;
    @Mock
    private RTMClientContext rtmClientContext;
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(message.getElementId()).thenReturn(elementId);
        when(message.getElementType()).thenReturn(elementType);
        when(message.getDescription()).thenReturn(description);
        when(message.getType()).thenReturn("project.courseware.description.set");
        when(coursewareService.getRootElementId(elementId, elementType)).thenReturn(Mono.just(rootElementId));
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(descriptiveChangeRTMProducer.buildDescriptiveChangeRTMConsumable(rtmClientContext, rootElementId, elementId, elementType, description))
                .thenReturn(descriptiveChangeRTMProducer);
    }

    @Test
    void validate_noElementId() {
        when(message.getElementId()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("elementId is required", t.getErrorMessage());
        assertEquals(COURSEWARE_DESCRIPTION_SET_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_noElementType() {
        when(message.getElementType()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("elementType is required", t.getErrorMessage());
        assertEquals(COURSEWARE_DESCRIPTION_SET_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_noDescription() {
        when(message.getDescription()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("description is required", t.getErrorMessage());
        assertEquals(COURSEWARE_DESCRIPTION_SET_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate() throws RTMValidationException {
        handler.validate(message);
    }

    @Test
    void handle_success() throws IOException {
        when(coursewareElementDescriptionService.createCoursewareElementDescription(elementId, elementType, description))
                .thenReturn(Mono.just(new CoursewareElementDescription()
                        .setElementId(elementId)
                        .setElementType(elementType)
                        .setValue(description)));

        handler.handle(session, message);

        verify(coursewareElementDescriptionService).createCoursewareElementDescription(elementId, elementType, description);

        String expected = "{\"type\":\"project.courseware.description.set.ok\"," +
                            "\"response\":{" +
                                "\"coursewareElementDescription\":{" +
                                    "\"elementId\":\"" + elementId + "\"," +
                                    "\"elementType\":\""+ elementType +"\"," +
                                    "\"value\":\""+ description +"\"}}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(descriptiveChangeRTMProducer, atLeastOnce()).buildDescriptiveChangeRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(elementId), eq(elementType), eq(description));
        verify(descriptiveChangeRTMProducer, atLeastOnce()).produce();
    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<CoursewareElementDescription> publisher = TestPublisher.create();
        publisher.error(new RuntimeException("some exception"));
        when(coursewareElementDescriptionService.createCoursewareElementDescription(elementId, elementType, description)).thenReturn(publisher.mono());

        handler.handle(session, message);

        verifySentMessage(session, "{\"type\":\"" + COURSEWARE_DESCRIPTION_SET_ERROR + "\",\"code\":422," +
                "\"message\":\"could not create courseware element description\"}");
        verify(descriptiveChangeRTMProducer, never()).produce();
    }

}
