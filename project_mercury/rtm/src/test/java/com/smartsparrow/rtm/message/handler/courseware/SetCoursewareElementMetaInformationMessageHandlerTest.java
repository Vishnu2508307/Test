package com.smartsparrow.rtm.message.handler.courseware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementMetaInformation;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.learner.service.CoursewareElementMetaInformationService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.SetCoursewareElementMetaInformationMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class SetCoursewareElementMetaInformationMessageHandlerTest {

    @InjectMocks
    private SetCoursewareElementMetaInformationMessageHandler handler;

    @Mock
    private CoursewareElementMetaInformationService coursewareElementMetaInformationService;

    @Mock
    private SetCoursewareElementMetaInformationMessage message;

    @Mock
    private Provider<RTMEventBroker> rtmEventBrokerProvider;

    @Mock
    private RTMEventBroker rtmEventBroker;

    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private static final UUID elementId = UUID.randomUUID();
    private static final CoursewareElementType elementType = CoursewareElementType.INTERACTIVE;
    private static final String key = "Cuccuruccucu'";
    private static final String value = "paloma";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(message.getElementId()).thenReturn(elementId);
        when(message.getElementType()).thenReturn(elementType);
        when(message.getKey()).thenReturn(key);
        when(message.getValue()).thenReturn(value);
        when(message.getType()).thenReturn("workspace.courseware.meta.info.set");

        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);
    }

    @Test
    void validate_noElementId() {
        when(message.getElementId()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("elementId is required", f.getMessage());
    }

    @Test
    void validate_noElementType() {
        when(message.getElementType()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("elementType is required", f.getMessage());
    }

    @Test
    void validate_noKey() {
        when(message.getKey()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("key is required", f.getMessage());
    }

    @Test
    void handle_success() throws WriteResponseException {
        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);

        when(coursewareElementMetaInformationService.createMetaInfo(elementId, key, value))
                .thenReturn(Mono.just(new CoursewareElementMetaInformation()
                        .setElementId(elementId)
                        .setKey(key)
                        .setValue(value)));

        handler.handle(session, message);

        String expected = "{" +
                "\"type\":\"workspace.courseware.meta.info.set.ok\"," +
                "\"response\":{" +
                    "\"coursewareElementMetaInformation\":{" +
                        "\"key\":\"Cuccuruccucu'\"," +
                        "\"value\":\"paloma\"," +
                        "\"elementId\":\"" + elementId + "\"}}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);

        verify(rtmEventBroker).broadcast(eq("workspace.courseware.meta.info.set"), captor.capture());

        CoursewareElementBroadcastMessage captured = captor.getValue();

        assertNotNull(captured);

        assertEquals(CoursewareAction.COURSEWARE_ELEMENT_META_INFO_CHANGED, captured.getAction());
        assertEquals(message.getElementId(), captured.getElement().getElementId());
        assertEquals(message.getElementType(), captured.getElement().getElementType());
    }

    @Test
    void handle_error() throws WriteResponseException {
        TestPublisher<CoursewareElementMetaInformation> publisher = TestPublisher.create();
        publisher.error(new Exception("!ops!"));
        when(coursewareElementMetaInformationService.createMetaInfo(elementId, key, value))
                .thenReturn(publisher.mono());

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"workspace.courseware.meta.info.set.error\"," +
                            "\"code\":422," +
                            "\"message\":\"could not create courseware element meta information\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        verify(rtmEventBroker, never()).broadcast(any(String.class), any(CoursewareElementBroadcastMessage.class));

    }
}