package com.smartsparrow.rtm.message.handler.courseware.scope;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.apache.http.HttpStatus;
import org.assertj.core.util.Lists;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.Feedback;
import com.smartsparrow.courseware.data.PluginReference;
import com.smartsparrow.courseware.data.ScopeReference;
import com.smartsparrow.courseware.lang.CoursewareElementNotFoundFault;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.courseware.scope.StudentScopeMessage;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class RegisterToStudentScopeMessageHandlerTest {

    @InjectMocks
    private RegisterToStudentScopeMessageHandler handler;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private StudentScopeMessage message;
    private static final String messageId = "messageId";
    private static final UUID elementId = UUID.randomUUID();
    private static final CoursewareElementType elementType = CoursewareElementType.FEEDBACK;
    private static final UUID studentScopeURN = UUID.randomUUID();
    private static final CoursewareElement element = new CoursewareElement()
            .setElementId(UUID.randomUUID())
            .setElementType(CoursewareElementType.INTERACTIVE);
    private static final Session session = RTMWebSocketTestUtils.mockSession();
    private static final UUID pluginId = UUID.randomUUID();
    private static final String pluginVersion = "1.2.1";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(message.getId()).thenReturn(messageId);
        when(message.getElementId()).thenReturn(elementId);
        when(message.getElementType()).thenReturn(elementType);
        when(message.getStudentScopeURN()).thenReturn(studentScopeURN);

        when(coursewareService.findElementByStudentScope(studentScopeURN))
                .thenReturn(Mono.just(element));
    }

    @Test
    @DisplayName("it requires element id to be not null")
    void validate_nullElementId() {
        when(message.getElementId()).thenReturn(null);
        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals(messageId, e.getReplyTo());
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
        assertEquals("author.student.scope.register.error", e.getType());
        assertEquals("elementId is required", e.getErrorMessage());
    }

    @Test
    @DisplayName("it requires element type to be not null")
    void validate_nullElementType() {
        when(message.getElementType()).thenReturn(null);
        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals(messageId, e.getReplyTo());
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
        assertEquals("author.student.scope.register.error", e.getType());
        assertEquals("elementType is required", e.getErrorMessage());
    }

    @Test
    @DisplayName("it requires student scope urn to be not null")
    void validate_nullStudentScopeURN() {
        when(message.getStudentScopeURN()).thenReturn(null);
        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals(messageId, e.getReplyTo());
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
        assertEquals("author.student.scope.register.error", e.getType());
        assertEquals("studentScopeURN is required", e.getErrorMessage());
    }

    @Test
    @DisplayName("it should throw an exception if the element is not found for the student scope")
    void validate_elementNotFoundForStudentScope() {
        TestPublisher<CoursewareElement> publisher = TestPublisher.create();
        publisher.error(new CoursewareElementNotFoundFault("foo"));

        when(coursewareService.findElementByStudentScope(studentScopeURN)).thenReturn(publisher.mono());

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals(messageId, e.getReplyTo());
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
        assertEquals("author.student.scope.register.error", e.getType());
        assertEquals("foo", e.getErrorMessage());
    }

    @Test
    @DisplayName("it should only allow to register to a scope within the parent path")
    void validate_studentScopeURN_doesNotBelongToTheSameParentPath() {
        when(coursewareService.getPath(elementId, CoursewareElementType.FEEDBACK))
                .thenReturn(Mono.just(Lists.newArrayList()));

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals(messageId, e.getReplyTo());
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
        assertEquals("author.student.scope.register.error", e.getType());
        assertEquals(String.format("cannot register element with student scope `%s` that is not in the parent path",
                studentScopeURN), e.getErrorMessage());
    }

    @Test
    @DisplayName("it should only allow to register an element with a plugin reference")
    void validate_elementNotAPluginReference() {
        when(message.getElementType()).thenReturn(CoursewareElementType.PATHWAY);

        when(coursewareService.getPath(elementId, CoursewareElementType.PATHWAY))
                .thenReturn(Mono.just(Lists.newArrayList(element)));

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals(messageId, e.getReplyTo());
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
        assertEquals("author.student.scope.register.error", e.getType());
        assertEquals("only elementType with a plugin reference can be registered", e.getErrorMessage());
    }

    @Test
    @DisplayName("it should throw an exception if the found element for the given scope is not a walkable")
    void validate_notAWalkable() {
        when(coursewareService.findElementByStudentScope(studentScopeURN))
                .thenReturn(Mono.just(new CoursewareElement().setElementType(CoursewareElementType.SCENARIO)));

        RTMValidationException e = assertThrows(RTMValidationException.class, () -> handler.validate(message));

        assertEquals(messageId, e.getReplyTo());
        assertEquals(HttpStatus.SC_BAD_REQUEST, e.getStatusCode());
        assertEquals("author.student.scope.register.error", e.getType());
        assertEquals(String.format("element associated with a student scope urn must be a walkable, found %s instead",
                CoursewareElementType.SCENARIO), e.getErrorMessage());
    }

    @Test
    void handle_success() throws WriteResponseException {
        when(coursewareService.findPluginReference(elementId, elementType)).thenReturn(Mono.just(new Feedback()
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersion)));

        when(coursewareService.register(eq(studentScopeURN), any(PluginReference.class), eq(elementId), eq(elementType)))
                .thenReturn(Mono.just(new ScopeReference()
                        .setElementId(elementId)
                        .setElementType(elementType)
                        .setPluginId(pluginId)
                        .setPluginVersion(pluginVersion)
                        .setScopeURN(studentScopeURN)));

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"author.student.scope.register.ok\"," +
                            "\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_fail_findPluginReference() throws WriteResponseException {
        TestPublisher<PluginReference> publisher = TestPublisher.create();
        publisher.error(new RuntimeException("fubar"));

        when(coursewareService.findPluginReference(elementId, elementType)).thenReturn(publisher.mono());

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"author.student.scope.register.error\"," +
                            "\"code\":422," +
                            "\"message\":\"error registering\"," +
                            "\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_fail_register() throws WriteResponseException {
        when(coursewareService.findPluginReference(elementId, elementType)).thenReturn(Mono.just(new Feedback()
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersion)));

        TestPublisher<ScopeReference> publisher = TestPublisher.create();
        publisher.error(new RuntimeException("fubar"));

        when(coursewareService.register(eq(studentScopeURN), any(PluginReference.class), eq(elementId), eq(elementType)))
                .thenReturn(publisher.mono());

        handler.handle(session, message);

        String expected = "{" +
                            "\"type\":\"author.student.scope.register.error\"," +
                            "\"code\":422," +
                            "\"message\":\"error registering\"," +
                            "\"replyTo\":\"messageId\"}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }
}
