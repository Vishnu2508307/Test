package com.smartsparrow.rtm.message.handler.courseware.annotation;

import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.courseware.annotation.UpdateCoursewareAnnotationMessageHandler.AUTHOR_ANNOTATION_UPDATE;
import static com.smartsparrow.rtm.message.handler.courseware.annotation.UpdateCoursewareAnnotationMessageHandler.AUTHOR_ANNOTATION_UPDATE_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.annotation.UpdateCoursewareAnnotationMessageHandler.AUTHOR_ANNOTATION_UPDATE_OK;
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
import com.smartsparrow.courseware.eventmessage.CoursewareAction;
import com.smartsparrow.courseware.eventmessage.CoursewareElementBroadcastMessage;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.annotation.UpdateCoursewareAnnotationMessage;
import com.smartsparrow.rtm.subscription.courseware.annotationupdated.AnnotationUpdatedRTMProducer;
import com.smartsparrow.util.Json;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class UpdateCoursewareAnnotationMessageHandlerTest {

    private Session session;

    @InjectMocks
    UpdateCoursewareAnnotationMessageHandler handler;

    @Mock
    AnnotationService annotationService;
    @Mock
    private UpdateCoursewareAnnotationMessage message;
    @Mock
    private AuthenticationContextProvider authenticationContextProvider;
    @Mock
    private Provider<RTMEventBroker> rtmEventBrokerProvider;
    @Mock
    private RTMEventBroker rtmEventBroker;
    @Mock
    private AnnotationUpdatedRTMProducer annotationUpdatedRTMProducer;
    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;
    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final Motivation motivation = Motivation.identifying;
    private static final UUID annotationId = UUIDs.timeBased();
    private static final UUID creatorId = UUID.randomUUID();
    private static final String body = "{\n\t\"body\": \"body\"\n}";
    private static final String target = "{\n\t\"target\": \"target\"\n}";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Account account = mock(Account.class);
        when(message.getElementId()).thenReturn(elementId);
        when(message.getElementType()).thenReturn(ACTIVITY);
        when(message.getAnnotationId()).thenReturn(annotationId);
        when(message.getBody()).thenReturn(body);
        when(message.getTarget()).thenReturn(target);
        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(annotationUpdatedRTMProducer.buildAnnotationUpdatedRTMConsumable(rtmClientContext, rootElementId, elementId, ACTIVITY, annotationId))
                .thenReturn(annotationUpdatedRTMProducer);

        session = mockSession();

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);

        handler = new UpdateCoursewareAnnotationMessageHandler(
                annotationService,
                rtmEventBrokerProvider,
                authenticationContextProvider,
                rtmClientContextProvider,
                annotationUpdatedRTMProducer);
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
    void validate_noAnnotationBody() {
        when(message.getBody()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing annotation body", ex.getMessage());
    }

    @Test
    void validate_noAnnotationTarget() {
        when(message.getTarget()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing annotation target", ex.getMessage());
    }

    @Test
    void validate_noAnnotationId() {
        when(message.getAnnotationId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing annotation id", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(annotationService.updateCoursewareAnnotation(any(), any(), any()))
                .thenReturn(Mono.just(new CoursewareAnnotation()
                        .setId(UUIDs.timeBased())
                        .setCreatorAccountId(creatorId)
                        .setMotivation(motivation)
                        .setRootElementId(rootElementId)
                        .setBodyJson(Json.toJsonNode(body))
                        .setTargetJson(Json.toJsonNode(target))
                        .setVersion(UUIDs.timeBased())
                        .setElementId(elementId)));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_ANNOTATION_UPDATE_OK, response.getType());
                Map responseMap = ((Map) response.getResponse().get("coursewareAnnotation"));
                assertEquals(rootElementId.toString(), responseMap.get("rootElementId"));
                assertEquals(motivation.toString(), responseMap.get("motivation"));
            });
        });

        ArgumentCaptor<CoursewareElementBroadcastMessage> captor = ArgumentCaptor.forClass(CoursewareElementBroadcastMessage.class);

        verify(rtmEventBroker).broadcast(eq(AUTHOR_ANNOTATION_UPDATE), captor.capture());
        final CoursewareElementBroadcastMessage broadcastMessage = captor.getValue();

        assertNotNull(broadcastMessage);
        assertEquals(CoursewareAction.ANNOTATION_UPDATED, broadcastMessage.getAction());
        assertEquals(CoursewareElement.from(message.getElementId(), message.getElementType()), broadcastMessage.getElement());
        assertEquals(accountId, broadcastMessage.getAccountId());

        verify(annotationUpdatedRTMProducer, atLeastOnce()).buildAnnotationUpdatedRTMConsumable(eq(rtmClientContext), eq(rootElementId), eq(elementId), eq(ACTIVITY), eq(annotationId));
        verify(annotationUpdatedRTMProducer, atLeastOnce()).produce();
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<CoursewareAnnotation> error = TestPublisher.create();
        error.error(new RuntimeException("some exception"));
        when(annotationService.updateCoursewareAnnotation(any(), any(), any()))
                .thenReturn(error.mono());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + AUTHOR_ANNOTATION_UPDATE_ERROR + "\",\"code\":422," +
                "\"message\":\"Unable to update courseware annotation\"}");
        verify(annotationUpdatedRTMProducer, never()).produce();
    }
}
