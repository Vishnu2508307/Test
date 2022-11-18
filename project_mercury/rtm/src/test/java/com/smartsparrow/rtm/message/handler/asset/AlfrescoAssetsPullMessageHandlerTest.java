package com.smartsparrow.rtm.message.handler.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.smartsparrow.iam.lang.UnauthorizedFault;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.sso.service.MyCloudService;
import com.smartsparrow.workspace.data.AlfrescoAssetSyncSummary;
import com.smartsparrow.workspace.service.AlfrescoAssetSyncType;
import com.smartsparrow.workspace.service.AlfrescoAssetTrackService;
import org.assertj.core.util.Lists;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.courseware.data.CoursewareElement;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.ext_http.service.RequestNotification;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.recv.asset.AlfrescoAssetsPullMessage;
import com.smartsparrow.workspace.service.AlfrescoAssetPullService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class AlfrescoAssetsPullMessageHandlerTest {

    @InjectMocks
    private AlfrescoAssetsPullMessageHandler handler;

    @Mock
    private AlfrescoAssetsPullMessage message;

    @Mock
    private AlfrescoAssetPullService alfrescoAssetPullService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private AlfrescoAssetTrackService alfrescoAssetTrackService;

    @Mock
    private MyCloudService myCloudService;

    private static final UUID timeBasedId = com.smartsparrow.util.UUIDs.timeBased();
    private static final UUID activityId = UUIDs.timeBased();
    private static final boolean forceAssetSync = false;
    private static final String myCloudToken = "DIOFJEOFGEROESCEOCJOVJ";
    private static final Session session = RTMWebSocketTestUtils.mockSession();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(message.getActivityId()).thenReturn(activityId);

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getPearsonToken()).thenReturn(myCloudToken);
    }

    @Test
    void validate_noActivityId() {
        when(message.getActivityId()).thenReturn(null);

        IllegalArgumentFault iaf = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertNotNull(iaf);
        assertEquals("activity id is required", iaf.getMessage());
    }

    @Test
    void validate_notARootActivity() {
        when(coursewareService.getPath(activityId, CoursewareElementType.ACTIVITY))
                .thenReturn(Mono.just(Lists.newArrayList(
                        CoursewareElement.from(UUID.randomUUID(), CoursewareElementType.ACTIVITY),
                        CoursewareElement.from(UUID.randomUUID(), CoursewareElementType.PATHWAY),
                        CoursewareElement.from(activityId, CoursewareElementType.ACTIVITY)
                )));

        IllegalArgumentFault iaf = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));

        assertEquals("invalid root activity id", iaf.getMessage());
    }

    @Test
    void handle_invalidToken() throws WriteResponseException {

        when(myCloudService.validateToken(myCloudToken)).thenThrow(new UnauthorizedFault("Invalid token supplied"));

        handler.handle(session, message);

        final String expected = "{" +
                "\"type\":\"author.alfresco.assets.sync.error\"," +
                "\"code\":401," +
                "\"response\":{" +
                "\"reason\":\"Invalid token supplied\"" +
                "}}";

        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
    }

    @Test
    void handle_success() throws WriteResponseException {
        when(myCloudService.validateToken(myCloudToken)).thenReturn(Mono.just("some pid"));

        try (MockedStatic<com.smartsparrow.util.UUIDs> mockedUUIDs = Mockito.mockStatic(com.smartsparrow.util.UUIDs.class)) {
            mockedUUIDs.when(com.smartsparrow.util.UUIDs::timeBased).thenReturn(timeBasedId);

            when(alfrescoAssetTrackService.setReferenceId(eq(activityId), any(UUID.class), eq(AlfrescoAssetSyncType.PULL)))
                    .thenReturn(Mono.just(timeBasedId));
            when(alfrescoAssetTrackService.saveAlfrescoAssetSyncSummary(any(AlfrescoAssetSyncSummary.class)))
                    .thenReturn(Flux.empty());
            when(alfrescoAssetPullService.pullAssets(any(UUID.class), eq(activityId), eq(myCloudToken), eq(false)))
                    .thenReturn(Flux.just(new RequestNotification()));

            handler.handle(session, message);

            final String expected = "{" +
                    "\"type\":\"author.alfresco.assets.sync.ok\"," +
                    "\"response\":{" +
                    "\"activityId\":\"" + activityId + "\"," +
                    "\"referenceId\":\"" + timeBasedId + "\"" +
                    "}}";

            verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expected);
        }
    }
}