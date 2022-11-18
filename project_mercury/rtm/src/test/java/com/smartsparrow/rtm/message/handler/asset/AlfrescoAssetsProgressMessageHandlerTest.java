package com.smartsparrow.rtm.message.handler.asset;

import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.asset.AlfrescoAssetsProgressMessage;
import com.smartsparrow.workspace.service.AlfrescoAssetSyncType;
import com.smartsparrow.workspace.service.AlfrescoAssetTrackService;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

import java.io.IOException;
import java.util.UUID;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.asset.AlfrescoAssetsProgressMessageHandler.AUTHOR_ALFRESCO_ASSETS_PROGRESS_ERROR;
import static com.smartsparrow.rtm.message.handler.asset.AlfrescoAssetsProgressMessageHandler.AUTHOR_ALFRESCO_ASSETS_PROGRESS_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class AlfrescoAssetsProgressMessageHandlerTest {
    private Session session;

    @InjectMocks
    AlfrescoAssetsProgressMessageHandler handler;

    @Mock
    private AlfrescoAssetTrackService alfrescoAssetTrackService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private AlfrescoAssetsProgressMessage message;

    private static final UUID courseId = UUID.randomUUID();
    private static final AlfrescoAssetSyncType syncType = AlfrescoAssetSyncType.PUSH;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(message.getActivityId()).thenReturn(courseId);
        when(message.getSyncType()).thenReturn(syncType);

        session = mockSession();

        handler = new AlfrescoAssetsProgressMessageHandler(
                alfrescoAssetTrackService,
                coursewareService);
    }

    @Test
    void validate_noSyncType() {
        when(message.getSyncType()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("sync type is required", ex.getMessage());
    }

    @Test
    void validate_noActivityId() {
        when(message.getActivityId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("course id is required", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(alfrescoAssetTrackService.isSyncInProgress(eq(courseId), eq(syncType)))
                .thenReturn(Mono.just(true));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_ALFRESCO_ASSETS_PROGRESS_OK, response.getType());
                assertEquals(courseId.toString(), response.getResponse().get("courseId"));
                assertEquals(syncType.toString(), response.getResponse().get("syncType"));
                assertEquals(true, response.getResponse().get("inProgress"));
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        Mono mono = TestPublisher.create().error(new RuntimeException("someException")).mono();
        when(alfrescoAssetTrackService.isSyncInProgress(eq(courseId), eq(syncType)))
                .thenReturn(mono);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + AUTHOR_ALFRESCO_ASSETS_PROGRESS_ERROR + "\",\"code\":422," +
                "\"message\":\"Unable to check if an alfresco sync is in progress\"}");
    }
}
