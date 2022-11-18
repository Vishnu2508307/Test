package com.smartsparrow.rtm.message.handler.courseware.annotation;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.courseware.annotation.DeleteCoursewareAnnotationMessageHandler.AUTHOR_ANNOTATION_DELETE;
import static com.smartsparrow.rtm.message.handler.courseware.annotation.DeleteCoursewareAnnotationMessageHandler.AUTHOR_ANNOTATION_DELETE_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.annotation.DeleteCoursewareAnnotationMessageHandler.AUTHOR_ANNOTATION_DELETE_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import javax.inject.Provider;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.annotation.service.CoursewareAnnotation;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.annotation.DeleteCoursewareAnnotationMessage;
import com.smartsparrow.rtm.subscription.courseware.annotationdeleted.AnnotationDeletedRTMProducer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class DeleteCoursewareAnnotationMessageHandlerTest {

    private Session session;

    @InjectMocks
    DeleteCoursewareAnnotationMessageHandler handler;
    @Mock
    AnnotationService annotationService;
    @Mock
    private DeleteCoursewareAnnotationMessage message;
    @Mock
    private CoursewareAnnotation annotation;
    @Mock
    private Provider<RTMEventBroker> rtmEventBrokerProvider;
    @Mock
    private RTMEventBroker rtmEventBroker;
    @Mock
    private AuthenticationContextProvider authenticationContextProvider;
    @Mock
    private AnnotationDeletedRTMProducer annotationDeletedRTMProducer;
    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;
    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID rootElementId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID annotationId = UUID.randomUUID();
    private static final CoursewareElementType coursewareElementType = ACTIVITY;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Account account = mock(Account.class);
        when(message.getElementId()).thenReturn(elementId);
        when(message.getAnnotationId()).thenReturn(annotationId);
        when(message.getElementType()).thenReturn(coursewareElementType);
        when(annotationService.findCoursewareAnnotation(annotationId)).thenReturn(Mono.just(annotation));
        when(annotation.getRootElementId()).thenReturn(rootElementId);
        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(annotationDeletedRTMProducer.buildAnnotationDeletedRTMConsumable(rtmClientContext, rootElementId, elementId, ACTIVITY, annotationId))
                .thenReturn(annotationDeletedRTMProducer);

        session = mockSession();

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);

        handler = new DeleteCoursewareAnnotationMessageHandler(
                annotationService,
                rtmEventBrokerProvider,
                authenticationContextProvider,
                rtmClientContextProvider,
                annotationDeletedRTMProducer);
    }

    @Test
    void validate_noAnnotationId() {
        when(message.getAnnotationId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing annotationId", ex.getMessage());
    }

    @Test
    void validate_noElementId() {
        when(message.getElementId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing elementId", ex.getMessage());
    }

    @Test
    void validate_noElementType() {
        when(message.getElementType()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing elementType", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(annotationService.deleteAnnotation(any(CoursewareAnnotation.class)))
                .thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_ANNOTATION_DELETE_OK, response.getType());
            });
        });

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);

        verify(rtmEventBroker).broadcast(eq(AUTHOR_ANNOTATION_DELETE), captor.capture());
        final CoursewareElementBroadcastMessage broadcastMessage = captor.getValue();

        assertNotNull(broadcastMessage);
        assertEquals(CoursewareAction.ANNOTATION_DELETED, broadcastMessage.getAction());
        assertEquals(CoursewareElement.from(elementId, coursewareElementType), broadcastMessage.getElement());
        assertEquals(accountId, broadcastMessage.getAccountId());

        verify(annotationDeletedRTMProducer, atLeastOnce()).buildAnnotationDeletedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(elementId), eq(ACTIVITY), eq(annotationId));
        verify(annotationDeletedRTMProducer, atLeastOnce()).produce();
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<Void> error = TestPublisher.create();
        error.error(new RuntimeException("can't delete"));
        when(annotationService.deleteAnnotation(any(CoursewareAnnotation.class)))
                .thenReturn(error.flux());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + AUTHOR_ANNOTATION_DELETE_ERROR + "\",\"code\":422," +
                "\"message\":\"error deleting the annotation\"}");
        verify(annotationDeletedRTMProducer, never()).produce();
    }

}
