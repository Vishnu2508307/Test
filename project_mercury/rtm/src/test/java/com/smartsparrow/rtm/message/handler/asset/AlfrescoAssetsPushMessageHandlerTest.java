package com.smartsparrow.rtm.message.handler.asset;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.smartsparrow.iam.lang.UnauthorizedFault;
import com.smartsparrow.sso.service.MyCloudService;
import com.smartsparrow.workspace.data.AlfrescoAssetSyncSummary;
import com.smartsparrow.workspace.service.AlfrescoAssetSyncType;
import com.smartsparrow.workspace.service.AlfrescoAssetTrackService;
import com.smartsparrow.asset.service.AlfrescoAssetService;
import com.smartsparrow.asset.service.AlfrescoNodeChildren;
import org.assertj.core.util.Lists;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.ext_http.service.RequestNotification;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.asset.AlfrescoAssetsPushMessage;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.workspace.service.AlfrescoAssetPushService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class AlfrescoAssetsPushMessageHandlerTest {

    @InjectMocks
    private AlfrescoAssetsPushMessageHandler alfrescoAssetsPushMessageHandler;

    @Mock
    private AlfrescoAssetsPushMessage message;

    @Mock
    private AlfrescoAssetService alfrescoAssetService;

    @Mock
    private AlfrescoAssetPushService alfrescoAssetPushService;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private AlfrescoAssetTrackService alfrescoAssetTrackService;

    @Mock
    private MyCloudService myCloudService;

    private static final UUID timeBasedId = UUIDs.timeBased();
    private static final UUID courseId = UUIDs.timeBased();
    private static final UUID alfrescoNodeId = UUIDs.timeBased();
    private static final String myCloudToken = "DIOFJEOFGEROESCEOCJOVJ";
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        when(message.getActivityId()).thenReturn(courseId);
        when(message.getAlfrescoNodeId()).thenReturn(alfrescoNodeId);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getPearsonToken()).thenReturn(myCloudToken);
    }

    @Test
    void validate_noCourseId() {

        when(message.getActivityId()).thenReturn(null);

        IllegalArgumentFault iaf = assertThrows(IllegalArgumentFault.class,
                () -> alfrescoAssetsPushMessageHandler.validate(message));

        assertNotNull(iaf);
        assertEquals("course id is required", iaf.getMessage());
    }

    @Test
    void validate_noAlfrescoNodeId() {

        when(message.getAlfrescoNodeId()).thenReturn(null);

        IllegalArgumentFault iaf = assertThrows(IllegalArgumentFault.class,
                () -> alfrescoAssetsPushMessageHandler.validate(message));

        assertNotNull(iaf);
        assertEquals("Alfresco node id is required", iaf.getMessage());
    }

    @Test
    void validate_notARootActivity() {

        when(coursewareService.getPath(courseId, CoursewareElementType.ACTIVITY))
                .thenReturn(Mono.just(Lists.newArrayList(
                        CoursewareElement.from(UUID.randomUUID(), CoursewareElementType.ACTIVITY),
                        CoursewareElement.from(courseId, CoursewareElementType.ACTIVITY)
                )));

        IllegalArgumentFault iaf = assertThrows(IllegalArgumentFault.class, () -> alfrescoAssetsPushMessageHandler.validate(message));

        assertNotNull(iaf);
        assertEquals("invalid root activity id", iaf.getMessage());
    }

    @Test
    void handle_invalidToken() throws WriteResponseException {

        when(myCloudService.validateToken(myCloudToken)).thenThrow(new UnauthorizedFault("Invalid token supplied"));

        alfrescoAssetsPushMessageHandler.handle(session, message);

        final String expected = "{" +
                "\"type\":\"author.alfresco.assets.push.error\"," +
                "\"code\":401," +
                "\"response\":{" +
                "\"reason\":\"Invalid token supplied\"" +
            "}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_success() throws WriteResponseException {

        when(myCloudService.validateToken(myCloudToken)).thenReturn(Mono.just("some pid"));

        try (MockedStatic<UUIDs> mockedUUIDs = Mockito.mockStatic(UUIDs.class)) {
            mockedUUIDs.when(UUIDs::timeBased).thenReturn(timeBasedId);

            when(alfrescoAssetService.getNodeChildren(eq(alfrescoNodeId.toString()), eq(myCloudToken)))
                    .thenReturn(Mono.just(new AlfrescoNodeChildren()));

            when(alfrescoAssetTrackService.setReferenceId(eq(courseId), any(UUID.class), eq(AlfrescoAssetSyncType.PUSH)))
                    .thenReturn(Mono.just(timeBasedId));

            when(alfrescoAssetTrackService.saveAlfrescoAssetSyncSummary(any(AlfrescoAssetSyncSummary.class)))
                    .thenReturn(Flux.empty());

            when(alfrescoAssetPushService.pushCourseAssets(any(UUID.class), eq(courseId), eq(myCloudToken), eq(alfrescoNodeId)))
                    .thenReturn(Mono.just(Lists.newArrayList(new RequestNotification(), new RequestNotification())));

            alfrescoAssetsPushMessageHandler.handle(session, message);

            final String expected = "{" +
                    "\"type\":\"author.alfresco.assets.push.ok\"," +
                    "\"response\":{" +
                    "\"activityId\":\"" + courseId + "\"," +
                    "\"referenceId\":\"" + timeBasedId + "\"" +
                    "}}";

            verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        }
    }
}