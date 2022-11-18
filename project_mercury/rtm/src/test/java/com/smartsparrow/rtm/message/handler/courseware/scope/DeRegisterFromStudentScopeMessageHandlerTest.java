package com.smartsparrow.rtm.message.handler.courseware.scope;

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

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.courseware.scope.StudentScopeMessage;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

class DeRegisterFromStudentScopeMessageHandlerTest {

    @InjectMocks
    private DeRegisterFromStudentScopeMessageHandler handler;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private StudentScopeMessage message;

    private static final String messageId = "messageId";
    private static final UUID studentScopeURN = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final CoursewareElementType elementType = CoursewareElementType.COMPONENT;
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(message.getId()).thenReturn(messageId);
        when(message.getStudentScopeURN()).thenReturn(studentScopeURN);
        when(message.getElementType()).thenReturn(elementType);
        when(message.getElementId()).thenReturn(elementId);
    }

    @Test
    void validate_nullStudentScope() {
        when(message.getStudentScopeURN()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertEquals("author.student.scope.deregister.error", e.getType());
        assertEquals("studentScopeURN is required", e.getErrorMessage());
        assertEquals(messageId, e.getReplyTo());
    }

    @Test
    void validate_nullElementId() {
        when(message.getElementId()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertEquals("author.student.scope.deregister.error", e.getType());
        assertEquals("elementId is required", e.getErrorMessage());
        assertEquals(messageId, e.getReplyTo());
    }

    @Test
    void validate_nullElementType() {
        when(message.getElementType()).thenReturn(null);

        RTMValidationException e = assertThrows(RTMValidationException.class, ()-> handler.validate(message));

        assertEquals("author.student.scope.deregister.error", e.getType());
        assertEquals("elementType is required", e.getErrorMessage());
        assertEquals(messageId, e.getReplyTo());
    }

    @Test
    void handle_success() throws WriteResponseException {
        when(coursewareService.deRegister(studentScopeURN, elementId)).thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        String expected = "{\"type\":\"author.student.scope.deregister.ok\",\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_fail() throws WriteResponseException {
        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.error(new RuntimeException("fubar"));

        when(coursewareService.deRegister(studentScopeURN, elementId)).thenReturn(publisher.flux());

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"author.student.scope.deregister.error\"," +
                            "\"code\":422," +
                            "\"message\":\"error de-registering\"," +
                            "\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}
