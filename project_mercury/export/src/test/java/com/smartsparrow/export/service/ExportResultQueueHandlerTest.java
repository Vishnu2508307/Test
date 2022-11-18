package com.smartsparrow.export.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.export.data.ExportAmbrosiaSnippet;
import com.smartsparrow.export.data.ExportResultNotification;
import com.smartsparrow.export.data.ExportStatus;
import com.smartsparrow.export.data.ExportSummary;

import reactor.core.publisher.Mono;

class ExportResultQueueHandlerTest {

    @InjectMocks
    private ExportResultQueueHandler handler;

    @Mock
    private ExportService exportService;

    @Mock
    private ExportResultBroker exportResultBroker;

    @Mock
    private ExportResultNotification exportResultNotification;

    private static final UUID exportId = UUID.randomUUID();
    private static final UUID notificationId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID workspaceId = UUID.randomUUID();
    private static final UUID projectId = UUID.randomUUID();
    private static final CoursewareElementType elementType = CoursewareElementType.ACTIVITY;
    private static final String snippet = "{\"foo\":\"bar\"}";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(exportResultNotification.getExportId()).thenReturn(exportId);
        when(exportResultNotification.getNotificationId()).thenReturn(notificationId);
        when(exportResultNotification.getAccountId()).thenReturn(accountId);
        when(exportResultNotification.getElementId()).thenReturn(elementId);
        when(exportResultNotification.getElementType()).thenReturn(elementType);
        when(exportResultNotification.getCompletedAt()).thenReturn(UUID.randomUUID());
        when(exportResultNotification.getProjectId()).thenReturn(projectId);
        when(exportResultNotification.getWorkspaceId()).thenReturn(workspaceId);
        when(exportResultNotification.getStatus()).thenReturn(ExportStatus.COMPLETED);
        when(exportResultNotification.getAmbrosiaSnippet()).thenReturn(snippet);
    }

    @Test
    void handle() {
        when(exportService.create(exportId, notificationId, elementId, elementType, accountId, snippet))
                .thenReturn(Mono.just(new ExportAmbrosiaSnippet()
                        .setAmbrosiaSnippet(snippet)
                        .setExportId(exportId)));

        when(exportService.processResultSnippet(any(ExportAmbrosiaSnippet.class)))
                .thenReturn(Mono.just(exportResultNotification));

        when(exportResultBroker.broadcast(exportId)).thenReturn(Mono.just(new ExportSummary()));

        handler.handle(exportResultNotification);

        verify(exportService).processResultSnippet(any(ExportAmbrosiaSnippet.class));
        verify(exportResultBroker).broadcast(exportId);
    }

}