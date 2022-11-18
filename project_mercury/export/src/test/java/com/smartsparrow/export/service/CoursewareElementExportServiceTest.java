package com.smartsparrow.export.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.export.data.ExportData;
import com.smartsparrow.export.data.ExportRequest;
import com.smartsparrow.export.data.ExportRequestNotification;
import com.smartsparrow.export.data.ExportResultNotification;
import com.smartsparrow.export.data.ExportType;
import com.smartsparrow.workspace.data.ProjectGateway;
import com.smartsparrow.workspace.data.WorkspaceProject;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CoursewareElementExportServiceTest {
    @InjectMocks
    private CoursewareElementExportService coursewareExportService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private ExportService exportService;

    @Mock
    private ProjectGateway projectGateway;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID projectId = UUID.randomUUID();
    private static final UUID elementId = UUID.randomUUID();
    private static final UUID rootElementId = UUID.randomUUID();
    private static final CoursewareElementType elementType = CoursewareElementType.ACTIVITY;
    private static final UUID workspaceId = UUID.randomUUID();
    private static final UUID exportId = UUID.randomUUID();
    private static final String metadata = "{\"landmarks\":{\"brief\":{\"title\":\"Halftitle\",\"id\":\"82efee30-377b-11ec-a679-5b1add49be01\"}}}";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(coursewareService.getWorkspaceId(elementId, CoursewareElementType.ACTIVITY)).thenReturn(Mono.just(workspaceId));
        when(coursewareService.getProjectId(elementId, CoursewareElementType.ACTIVITY)).thenReturn(Mono.just(projectId));
        when(projectGateway.findWorkspaceForProject(projectId)).thenReturn(Mono.just(new WorkspaceProject()
                .setProjectId(projectId)
                .setWorkspaceId(workspaceId)));
    }

    @Test
    public void create_success() {
        ArgumentCaptor<ExportRequestNotification> captor = ArgumentCaptor.forClass(ExportRequestNotification.class);

        when(exportService.submit(any(ExportRequest.class),
                                  eq(ExportType.EPUB_PREVIEW))).thenReturn(Flux.just(new ExportResultNotification()
                                                                                             .setExportId(exportId)));
        when(coursewareService.getRootElementId(elementId, elementType)).thenReturn(Mono.just(rootElementId));
        ExportData resultExportId = coursewareExportService.create(elementId, elementType, accountId, ExportType.EPUB_PREVIEW, metadata).block();

        assertNotNull(resultExportId);

        verify(coursewareService).getProjectId(elementId, CoursewareElementType.ACTIVITY);
        verify(projectGateway).findWorkspaceForProject(projectId);
        verify(exportService).submit(captor.capture(), eq(ExportType.EPUB_PREVIEW));

    }
}
