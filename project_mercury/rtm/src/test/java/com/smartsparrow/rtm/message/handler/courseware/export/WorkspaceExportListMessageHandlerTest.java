package com.smartsparrow.rtm.message.handler.courseware.export;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockSession;
import static com.smartsparrow.rtm.message.handler.courseware.export.WorkspaceExportListMessageHandler.WORKSPACE_EXPORT_LIST_ERROR;
import static com.smartsparrow.rtm.message.handler.courseware.export.WorkspaceExportListMessageHandler.WORKSPACE_EXPORT_LIST_OK;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.export.data.ExportStatus;
import com.smartsparrow.export.data.ExportSummary;
import com.smartsparrow.export.data.ExportType;
import com.smartsparrow.export.service.ExportService;
import com.smartsparrow.rtm.MessageHandlerTestUtils;
import com.smartsparrow.rtm.message.recv.courseware.activity.WorkspaceGenericMessage;

import reactor.core.publisher.Flux;
import reactor.test.publisher.TestPublisher;

public class WorkspaceExportListMessageHandlerTest {
    private Session session;

    @InjectMocks
    WorkspaceExportListMessageHandler handler;

    @Mock
    private ExportService exportService;

    @Mock
    private WorkspaceGenericMessage message;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID projectId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID workspaceId = UUID.randomUUID();
    private static final UUID exportId = UUID.randomUUID();
    private static final CoursewareElementType elementType = CoursewareElementType.ACTIVITY;
    private static final ExportStatus status = ExportStatus.IN_PROGRESS;
    private static final String ambrosiaUrl = "http://test.example";
    private static final UUID completedAt = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(message.getWorkspaceId()).thenReturn(workspaceId);
        session = mockSession();
        handler = new WorkspaceExportListMessageHandler(exportService);
    }

    @Test
    void validate_noWorkspaceId() {
        when(message.getWorkspaceId()).thenReturn(null);
        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("missing workspaceId", ex.getMessage());
    }

    @Test
    void handle() throws IOException {
        when(exportService.fetchExportSummariesForWorkspace(workspaceId))
                .thenReturn(Flux.just(new ExportSummary()
                                              .setId(exportId)
                                              .setAccountId(accountId)
                                              .setAmbrosiaUrl(ambrosiaUrl)
                                              .setCompletedAt(completedAt)
                                              .setElementId(elementId)
                                              .setElementType(elementType)
                                              .setProjectId(projectId)
                                              .setStatus(status)
                                              .setWorkspaceId(workspaceId)
                                              .setExportType(ExportType.EPUB_PREVIEW)));

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, response -> {
            assertAll(() -> {
                assertEquals(WORKSPACE_EXPORT_LIST_OK, response.getType());
                List responseList = ((List) response.getResponse().get("exportSummaries"));
                assertEquals(workspaceId.toString(), ((LinkedHashMap) responseList.get(0)).get("workspaceId"));
                assertEquals(projectId.toString(), ((LinkedHashMap) responseList.get(0)).get("projectId"));
                assertEquals(exportId.toString(), ((LinkedHashMap) responseList.get(0)).get("id"));
                assertEquals(ExportType.EPUB_PREVIEW.name(), ((LinkedHashMap) responseList.get(0)).get("exportType"));
            });
        });
    }

    @Test
    void handle_exception() throws IOException {
        TestPublisher<ExportSummary> error = TestPublisher.create();
        error.error(new RuntimeException("some exception"));
        when(exportService.fetchExportSummariesForWorkspace(workspaceId))
                .thenReturn(error.flux());

        handler.handle(session, message);

        MessageHandlerTestUtils.verifySentMessage(session, "{\"type\":\"" + WORKSPACE_EXPORT_LIST_ERROR + "\",\"code\":422," +
                "\"message\":\"Unable to fetch exports for workspace\"}");
    }
}
