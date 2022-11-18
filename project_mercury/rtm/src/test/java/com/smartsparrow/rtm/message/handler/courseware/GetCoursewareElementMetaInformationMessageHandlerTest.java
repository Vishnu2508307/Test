package com.smartsparrow.rtm.message.handler.courseware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementMetaInformation;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.learner.service.CoursewareElementMetaInformationService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.courseware.GetCoursewareElementMetaInformationMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class GetCoursewareElementMetaInformationMessageHandlerTest {

    @InjectMocks
    private GetCoursewareElementMetaInformationMessageHandler handler;

    @Mock
    private GetCoursewareElementMetaInformationMessage message;

    @Mock
    private CoursewareElementMetaInformationService coursewareElementMetaInformationService;

    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private static final UUID elementId = UUID.randomUUID();
    private static final CoursewareElementType elementType = CoursewareElementType.INTERACTIVE;
    private static final String key = "motogp";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(message.getElementId()).thenReturn(elementId);
        when(message.getElementType()).thenReturn(elementType);
        when(message.getKey()).thenReturn(key);
    }

    @Test
    void validate_missingElementId() {
        when(message.getElementId()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("elementId is required", f.getMessage());
    }

    @Test
    void validate_missingElementType() {
        when(message.getElementType()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("elementType is required", f.getMessage());
    }

    @Test
    void validate_missingKey() {
        when(message.getKey()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("key is required", f.getMessage());
    }

    @Test
    void handle_success() throws WriteResponseException {
        when(coursewareElementMetaInformationService.findMetaInfo(elementId, key))
                .thenReturn(Mono.just(new CoursewareElementMetaInformation()
                        .setValue("ValentinoRossi")
                        .setKey(key)
                        .setElementId(elementId)));

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"workspace.courseware.meta.info.get.ok\"," +
                            "\"response\":{" +
                                "\"coursewareElementMetaInformation\":{" +
                                    "\"key\":\"motogp\"," +
                                    "\"value\":\"ValentinoRossi\"," +
                                    "\"elementId\":\"" + elementId + "\"}}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<CoursewareElementMetaInformation> publisher = TestPublisher.create();
        publisher.error(new Exception("PhilipIsland"));
        when(coursewareElementMetaInformationService.findMetaInfo(elementId, key))
                .thenReturn(publisher.mono());

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"workspace.courseware.meta.info.get.error\"," +
                            "\"code\":422," +
                            "\"message\":\"could not fetch courseware element meta information\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}