package com.smartsparrow.rtm.message.handler.courseware.activity;

import static com.smartsparrow.courseware.data.CoursewareElementType.PATHWAY;
import static com.smartsparrow.courseware.pathway.PathwayMock.mockPathway;
import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.courseware.activity.DuplicateActivityMessageHandler.AUTHOR_ACTIVITY_DUPLICATE_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.activity.DuplicateActivityMessageHandler.AUTHOR_ACTIVITY_DUPLICATE_OK;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.google.inject.Provider;
import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.ActivityConfig;
import com.smartsparrow.courseware.data.ActivityTheme;
import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.lang.ActivityNotFoundException;
import com.smartsparrow.courseware.lang.PathwayNotFoundException;
import com.smartsparrow.courseware.pathway.Pathway;
import com.smartsparrow.courseware.payload.ActivityPayload;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.RTMValidationException;
import com.smartsparrow.rtm.message.RTMClientContext;
import com.smartsparrow.rtm.message.event.RTMEventBroker;
import com.smartsparrow.rtm.message.recv.courseware.activity.DuplicateActivityMessage;
import com.smartsparrow.rtm.subscription.courseware.duplicated.ActivityDuplicatedRTMProducer;
import com.smartsparrow.workspace.data.ThemePayload;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;
import reactor.util.function.Tuple2;

class DuplicateActivityMessageHandlerTest {

    private DuplicateActivityMessageHandler handler;
    @Mock
    private ActivityService activityService;
    @Mock
    private PathwayService pathwayService;
    @Mock
    private CoursewareService coursewareService;
    private Session session;
    @Mock
    private DuplicateActivityMessage message;

    private Provider<RTMEventBroker> rtmEventBrokerProvider;
    private RTMEventBroker rtmEventBroker;
    @Mock
    private ActivityDuplicatedRTMProducer activityDuplicatedRTMProducer;
    @Mock
    private Provider<RTMClientContext> rtmClientContextProvider;
    @Mock
    private RTMClientContext rtmClientContext;

    private static final UUID activityId = UUIDs.timeBased();
    private static final UUID accountId = UUIDs.timeBased();
    private static final UUID parentPathwayId = UUIDs.timeBased();
    private static final UUID rootElementId = UUIDs.timeBased();

    private Account account;
    private Boolean newDuplicateFlow = false;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = RTMWebSocketTestUtils.mockSession();
        account = mock(Account.class);
        Tuple2<Provider<RTMEventBroker>, RTMEventBroker> mockRTMEventBroker =
                RTMWebSocketTestUtils.mockProvidedClass(RTMEventBroker.class);

        rtmEventBroker = mockRTMEventBroker.getT2();
        rtmEventBrokerProvider = mockRTMEventBroker.getT1();

        Tuple2<Provider<AuthenticationContext>, AuthenticationContext> mockAuthenticationContextProvider = RTMWebSocketTestUtils
                .mockProvidedClass(AuthenticationContext.class);

        Provider<AuthenticationContext> authenticationContextProvider = mockAuthenticationContextProvider.getT1();
        AuthenticationContext authenticationContext = mockAuthenticationContextProvider.getT2();

        when(authenticationContext.getAccount()).thenReturn(account);

        when(message.getActivityId()).thenReturn(activityId);
        when(message.getParentPathwayId()).thenReturn(parentPathwayId);
        when(message.getIndex()).thenReturn(null);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);

        when(rtmEventBrokerProvider.get()).thenReturn(rtmEventBroker);
        when(rtmClientContextProvider.get()).thenReturn(rtmClientContext);
        when(coursewareService.getRootElementId(parentPathwayId, PATHWAY)).thenReturn(Mono.just(rootElementId));

        handler = new DuplicateActivityMessageHandler(
                activityService,
                pathwayService,
                coursewareService,
                authenticationContextProvider,
                rtmEventBrokerProvider,
                rtmClientContextProvider,
                activityDuplicatedRTMProducer
        );
    }

    @Test
    void validate_noActivityId() {
        when(message.getActivityId()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("missing activityId", t.getErrorMessage());
        assertEquals(AUTHOR_ACTIVITY_DUPLICATE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_activityNotFound() {
        Mono<Activity> mono = TestPublisher.<Activity>create().error(new ActivityNotFoundException(activityId)).mono();
        when(activityService.findById(message.getActivityId())).thenReturn(mono);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("activity not found", t.getErrorMessage());
        assertEquals(AUTHOR_ACTIVITY_DUPLICATE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_noParentPathwayId() {
        when(message.getParentPathwayId()).thenReturn(null);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("missing parentPathwayId", t.getErrorMessage());
        assertEquals(AUTHOR_ACTIVITY_DUPLICATE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_parentPathwayNotFound() {
        when(activityService.findById(message.getActivityId())).thenReturn(Mono.just(new Activity()));
        Mono<Pathway> mono = TestPublisher.<Pathway>create().error(new PathwayNotFoundException(parentPathwayId)).mono();
        when(pathwayService.findById(message.getParentPathwayId())).thenReturn(mono);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("parent pathway not found", t.getErrorMessage());
        assertEquals(AUTHOR_ACTIVITY_DUPLICATE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_negativeIndex() {
        when(activityService.findById(message.getActivityId())).thenReturn(Mono.just(new Activity()));
        Pathway pathway = mockPathway();
        when(pathwayService.findById(message.getParentPathwayId())) //
                .thenReturn(Mono.just(pathway));
        when(message.getIndex()).thenReturn(-1);

        RTMValidationException t = assertThrows(RTMValidationException.class, () -> handler.validate(message));
        assertEquals("index should be >= 0", t.getErrorMessage());
        assertEquals(AUTHOR_ACTIVITY_DUPLICATE_ERROR, t.getType());
        assertEquals(400, t.getStatusCode());
    }

    @Test
    void validate_positiveIndex() throws RTMValidationException {
        when(activityService.findById(message.getActivityId())).thenReturn(Mono.just(new Activity()));
        Pathway pathway = mockPathway();
        when(pathwayService.findById(message.getParentPathwayId())) //
                .thenReturn(Mono.just(pathway));
        when(message.getIndex()).thenReturn(1);

        handler.validate(message);
    }

    @Test
    void validate() throws RTMValidationException {
        when(activityService.findById(message.getActivityId())).thenReturn(Mono.just(new Activity()));
        Pathway pathway = mockPathway();
        when(pathwayService.findById(message.getParentPathwayId())) //
                .thenReturn(Mono.just(pathway));
        handler.validate(message);
    }

    @Test
    void handle() throws IOException {
        UUID newActivityId = UUIDs.timeBased();
        Activity newActivity = new Activity().setId(newActivityId);
        ActivityPayload expectedPayload = ActivityPayload.from(newActivity,
                                                               new ActivityConfig(),
                                                               new PluginSummary(),
                                                               new AccountPayload(),
                                                               new ActivityTheme(),
                                                               new ArrayList<>(),
                                                               new ArrayList<>(),
                                                               new CoursewareElementDescription(),
                                                               new ArrayList<>(),
                                                               new ThemePayload(),
                                                               Collections.emptyList());
        when(coursewareService.duplicateActivity(activityId, parentPathwayId, account, newDuplicateFlow)).thenReturn(
                Mono.just(newActivity));
        when(activityService.getActivityPayload(newActivity)).thenReturn(Mono.just(expectedPayload));
        when(activityDuplicatedRTMProducer.buildActivityDuplicatedRTMConsumable(rtmClientContext,
                                                                                rootElementId,
                                                                                newActivityId,
                                                                                parentPathwayId))
                .thenReturn(activityDuplicatedRTMProducer);

        handler.handle(session, message);

        verifySentMessage(session, response -> assertAll(() -> {
            assertEquals(AUTHOR_ACTIVITY_DUPLICATE_OK, response.getType());
            Map responseMap = ((Map) response.getResponse().get("activity"));
            assertEquals(newActivityId.toString(), responseMap.get("activityId"));
            assertNotNull(responseMap.get("createdAt"));
        }));
        verify(activityDuplicatedRTMProducer, atLeastOnce()).buildActivityDuplicatedRTMConsumable(eq(rtmClientContext),
                                                                                                  eq(rootElementId),
                                                                                                  eq(newActivityId),
                                                                                                  eq(parentPathwayId));
        verify(activityDuplicatedRTMProducer, atLeastOnce()).produce();
    }

    @Test
    void handle_newDuplicateFlowIsON() throws IOException {
        newDuplicateFlow = true;

        UUID newActivityId = UUIDs.timeBased();
        Activity newActivity = new Activity().setId(newActivityId);
        ActivityPayload expectedPayload = ActivityPayload.from(newActivity,
                                                               new ActivityConfig(),
                                                               new PluginSummary(),
                                                               new AccountPayload(),
                                                               new ActivityTheme(),
                                                               new ArrayList<>(),
                                                               new ArrayList<>(),
                                                               new CoursewareElementDescription(),
                                                               new ArrayList<>(),
                                                               new ThemePayload(),
                                                               Collections.emptyList());
        when(coursewareService.duplicateActivity(activityId, parentPathwayId, account, newDuplicateFlow)).thenReturn(
                Mono.just(newActivity));
        when(activityService.getActivityPayload(newActivity)).thenReturn(Mono.just(expectedPayload));
        when(message.getNewDuplicateFlow()).thenReturn(newDuplicateFlow);
        when(activityDuplicatedRTMProducer.buildActivityDuplicatedRTMConsumable(rtmClientContext,
                                                                                rootElementId,
                                                                                newActivityId,
                                                                                parentPathwayId))
                .thenReturn(activityDuplicatedRTMProducer);

        handler.handle(session, message);

        verifySentMessage(session, response -> assertAll(() -> {
            assertEquals(AUTHOR_ACTIVITY_DUPLICATE_OK, response.getType());
            Map responseMap = ((Map) response.getResponse().get("activity"));
            assertEquals(newActivityId.toString(), responseMap.get("activityId"));
            assertNotNull(responseMap.get("createdAt"));
        }));
    }

    @Test
    void handle_withIndex() throws IOException {
        when(message.getIndex()).thenReturn(1);
        UUID newActivityId = UUIDs.timeBased();
        Activity newActivity = new Activity().setId(newActivityId);
        when(coursewareService.duplicateActivity(activityId, parentPathwayId, 1, account, newDuplicateFlow)).thenReturn(Mono.just(newActivity));
        when(activityService.getActivityPayload(newActivity)).thenReturn(Mono.just(new ActivityPayload()));
        when(activityDuplicatedRTMProducer.buildActivityDuplicatedRTMConsumable(rtmClientContext, rootElementId, null, parentPathwayId))
                .thenReturn(activityDuplicatedRTMProducer);

        handler.handle(session, message);

        verifySentMessage(session, response -> assertAll(() -> {
            assertEquals(AUTHOR_ACTIVITY_DUPLICATE_OK, response.getType());
        }));
    }


    @Test
    void handle_exception() throws IOException {
        TestPublisher<Activity> error = TestPublisher.create();
        error.error(new RuntimeException("some exception"));
        when(coursewareService.duplicateActivity(activityId, parentPathwayId, account, newDuplicateFlow)).thenReturn(error.mono());
        when(activityService.getActivityPayload(any(Activity.class))).thenReturn(Mono.empty());

        handler.handle(session, message);

        verifySentMessage(session,
                "{\"type\":\"" + AUTHOR_ACTIVITY_DUPLICATE_ERROR + "\",\"code\":422,\"message\":\"Unable to duplicate activity\"}");
        verify(activityDuplicatedRTMProducer, never()).produce();
    }

    @Test
    void handle_activityNotFound() throws IOException {
        TestPublisher<Activity> error = TestPublisher.create();
        error.error(new ActivityNotFoundException(activityId));
        when(coursewareService.duplicateActivity(activityId, parentPathwayId, account, newDuplicateFlow)).thenReturn(error.mono());
        when(activityService.getActivityPayload(any(Activity.class))).thenReturn(Mono.empty());

        handler.handle(session, message);

        verifySentMessage(session,
                "{\"type\":\"" + AUTHOR_ACTIVITY_DUPLICATE_ERROR + "\",\"code\":404,\"message\":\"Activity not found\"}");
        verify(activityDuplicatedRTMProducer, never()).produce();
    }
}
