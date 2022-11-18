package com.smartsparrow.rtm.message.handler.courseware.annotation;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.courseware.annotation.CreateCoursewareAnnotationMessageHandler.AUTHOR_ANNOTATION_CREATE;
import static com.smartsparrow.rtm.message.handler.courseware.annotation.CreateCoursewareAnnotationMessageHandler.AUTHOR_ANNOTATION_CREATE_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.annotation.CreateCoursewareAnnotationMessageHandler.AUTHOR_ANNOTATION_CREATE_OK;
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
import java.util.Map;
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
import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.annotation.CreateCoursewareAnnotationMessage;
import com.smartsparrow.rtm.subscription.courseware.annotationcreated.AnnotationCreatedRTMProducer;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

public class CreateCoursewareAnnotationMessageHandlerTest {

    private Session session;

    @InjectMocks
    CreateCoursewareAnnotationMessageHandler handler;
    @Mock
    AnnotationService annotationService;
    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;
    @Mock
    private CreateCoursewareAnnotationMessage message;
    @Mock
    private Provider<RTMEventBroker> rtmEventBrokerProvider;
    @Mock
    private RTMEventBroker rtmEventBroker;
    @Mock
    private AnnotationCreatedRTMProducer annotationCreatedRTMProducer;
    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;
    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID annotationId = UUID.randomUUID();
    private static final CoursewareElementType coursewareElementType = CoursewareElementType.ACTIVITY;
    private static final Motivation motivation = Motivation.identifying;
    private static final String body = "{\n\t\"body\": \"body\"\n}";
    private static final String target = "{\n\t\"target\": \"target\"\n}";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Account account = mock(Account.class);
        when(message.getElementId()).thenReturn(elementId);
        when(message.getRootElementId()).thenReturn(rootElementId);
        when(message.getElementType()).thenReturn(coursewareElementType);
        when(message.getMotivation()).thenReturn(motivation);
        when(message.getBody()).thenReturn(body);
        when(message.getTarget()).thenReturn(target);
        when(message.getAnnotationId()).thenReturn(annotationId);
        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(annotationCreatedRTMProducer.buildAnnotationCreatedRTMConsumable(rtmClientContext, rootElementId, elementId, coursewareElementType, annotationId))
                .thenReturn(annotationCreatedRTMProducer);

        session = mockSession();

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);

        handler = new CreateCoursewareAnnotationMessageHandler(
                annotationService,
                rtmEventBrokerProvider,
                authenticationContextProvider,
                rtmClientContextProvider,
                annotationCreatedRTMProducer);
    }

    @Test
    void validate_noRootElementId() {
        when(message.getRootElementId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing rootElementId", ex.getMessage());
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
    void validate_noMotivation() {
        when(message.getMotivation()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing motivation", ex.getMessage());
    }

    @Test
    void validate_noBody() {
        when(message.getBody()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing body", ex.getMessage());
    }

    @Test
    void validate_noTarget() {
        when(message.getTarget()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing target", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(annotationService.create(any(CoursewareAnnotation.class), any(UUID.class)))
                .thenReturn(Flux.just(new Void[]{}));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_ANNOTATION_CREATE_OK, response.getType());
                Map responseMap = ((Map) response.getResponse().get("coursewareAnnotation"));
                assertEquals(elementId.toString(), responseMap.get("elementId"));
                assertEquals(rootElementId.toString(), responseMap.get("rootElementId"));
                assertEquals(motivation.toString(), responseMap.get("motivation"));
            });
        });

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);

        verify(rtmEventBroker).broadcast(eq(AUTHOR_ANNOTATION_CREATE), captor.capture());
        final CoursewareElementBroadcastMessage broadcastMessage = captor.getValue();

        assertNotNull(broadcastMessage);
        assertEquals(CoursewareAction.ANNOTATION_CREATED, broadcastMessage.getAction());
        assertEquals(CoursewareElement.from(elementId, coursewareElementType), broadcastMessage.getElement());
        assertEquals(accountId, broadcastMessage.getAccountId());

        verify(annotationCreatedRTMProducer, atLeastOnce()).buildAnnotationCreatedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(elementId), eq(coursewareElementType), eq(annotationId));
        verify(annotationCreatedRTMProducer, atLeastOnce()).produce();
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<Void> error = TestPublisher.create();
        error.error(new RuntimeException("can't create"));
        when(annotationService.create(any(CoursewareAnnotation.class), any(UUID.class)))
                .thenReturn(error.flux());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + AUTHOR_ANNOTATION_CREATE_ERROR + "\",\"code\":422," +
                "\"message\":\"error creating the annotation\"}");
        verify(annotationCreatedRTMProducer, never()).produce();
    }

}
