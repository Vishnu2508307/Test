package com.smartsparrow.rtm.message.handler.courseware.export;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.courseware.export.ActivityExportRequestMessageHandler.AUTHOR_EXPORT_REQUEST_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.export.ActivityExportRequestMessageHandler.AUTHOR_EXPORT_REQUEST_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

import java.io.IOException;
import java.util.UUID;

import javax.inject.Provider;

import com.smartsparrow.export.data.ExportData;
import com.smartsparrow.export.data.ExportType;
import com.smartsparrow.iam.service.WebSessionToken;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.export.data.ExportResultNotification;
import com.smartsparrow.export.service.CoursewareElementExportService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.courseware.export.CreateCoursewareRootElementExportMessage;
import com.smartsparrow.workspace.data.ProjectGateway;
import com.smartsparrow.workspace.data.WorkspaceProject;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class ActivityExportRequestMessageHandlerTest {
    private Session session;

    @InjectMocks
    ActivityExportRequestMessageHandler handler;

    @Mock
    private CoursewareElementExportService coursewareExportService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private ProjectGateway projectGateway;

    @Mock
    private Provider<AuthenticationContext> authenticationContextProvider;

    @Mock
    private CreateCoursewareRootElementExportMessage message;

    @Mock
    private WebSessionToken webSessionToken;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID projectId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID workspaceId = UUID.randomUUID();
    private static final UUID exportId = UUID.randomUUID();
    private static final Integer exportElementsCount = 1;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Account account = mock(Account.class);

        when(message.getElementId()).thenReturn(elementId);
        when(message.getExportType()).thenReturn(ExportType.EPUB_PREVIEW);
        when(message.getMetadata()).thenReturn("metadata");

        session = mockSession();

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(accountId);
        when(authenticationContext.getWebSessionToken()).thenReturn(webSessionToken);

        handler = new ActivityExportRequestMessageHandler(
                coursewareExportService,
                coursewareService,
                authenticationContextProvider,
                projectGateway);
    }

    @Test
    void validate_noElementId() {
        when(message.getElementId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing elementId", ex.getMessage());
    }

    @Test
    void validate_noProjectId() {
        when(coursewareService.getProjectId(message.getElementId(),CoursewareElementType.ACTIVITY)).thenReturn(Mono.empty());
        when(projectGateway.findWorkspaceForProject(projectId)).thenReturn(Mono.just(new WorkspaceProject()
        .setWorkspaceId(workspaceId)
        .setProjectId(projectId)));
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing projectId", ex.getMessage());
    }

    @Test
    void validate_noWorkspaceId() {
        when(coursewareService.getProjectId(message.getElementId(),CoursewareElementType.ACTIVITY)).thenReturn(Mono.just(projectId));
        when(projectGateway.findWorkspaceForProject(any(UUID.class))).thenReturn(Mono.just(new WorkspaceProject()));
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing workspaceId", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(coursewareExportService.create(any(UUID.class), any(CoursewareElementType.class), any(UUID.class),
                eq(ExportType.EPUB_PREVIEW), anyString()))
                .thenReturn(Mono.just(new ExportData()
                                              .setExportId(exportId)
                                              .setElementsExportedCount(exportElementsCount)));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(AUTHOR_EXPORT_REQUEST_OK, response.getType());
                assertEquals(exportId.toString(), response.getResponse().get("exportId"));
                assertEquals(exportElementsCount, response.getResponse().get("exportElementsCount"));
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<ExportResultNotification> error = TestPublisher.create();
        error.error(new RuntimeException("can't create"));
        Mono mono = TestPublisher.create().error(new RuntimeException("someException")).mono();
        when(coursewareExportService.create(any(UUID.class), any(CoursewareElementType.class), any(UUID.class), eq(ExportType.EPUB_PREVIEW), anyString()))
                .thenReturn(mono);

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + AUTHOR_EXPORT_REQUEST_ERROR + "\",\"code\":422," +
                "\"message\":\"Unable to create courseware activity export\"}");
    }
}
