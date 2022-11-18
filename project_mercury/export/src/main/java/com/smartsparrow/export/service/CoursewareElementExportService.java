package com.smartsparrow.export.service;


import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.newrelic.api.agent.Trace;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.service.CoursewareService;
import com.smartsparrow.export.data.ExportData;
import com.smartsparrow.export.data.ExportRequest;
import com.smartsparrow.export.data.ExportStatus;
import com.smartsparrow.export.data.ExportType;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;
import com.smartsparrow.util.monitoring.ReactiveTransaction;
import com.smartsparrow.workspace.data.ProjectGateway;

import reactor.core.publisher.Mono;

@Singleton
public class CoursewareElementExportService {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(CoursewareElementExportService.class);

    private final CoursewareService coursewareService;
    private final ExportService exportService;
    private final ProjectGateway projectGateway;

    @Inject
    public CoursewareElementExportService(final CoursewareService coursewareService,
                                          final ExportService exportService,
                                          final ProjectGateway projectGateway) {
        this.coursewareService = coursewareService;
        this.exportService = exportService;
        this.projectGateway = projectGateway;
    }

    /**
     * Create courseware element export request
     *
     * @param elementId the element id
     * @param accountId the account id
     * @param exportType the export type
     * @return mono of export data
     */
    @Trace(async = true)
    public Mono<ExportData> create(final UUID elementId,
                                   final CoursewareElementType elementType,
                                   final UUID accountId,
                                   ExportType exportType,
                                   final String metadata) {
        return coursewareService.getProjectId(elementId, elementType)
                .flatMap(projectGateway::findWorkspaceForProject)
                .doOnEach(ReactiveTransaction.linkOnNext())
                .doOnEach(log.reactiveInfoSignal("preparing courseware export request",
                                                 workspaceProject -> new HashMap<String, Object>() {{
                                                     put("elementId", elementId);
                                                     put("elementType", elementType);
                                                     put("accountId", accountId);
                                                 }}))
                .flatMap(workspaceProject -> coursewareService.getRootElementId(elementId, elementType)
                        .flatMap(rootElementId -> {
                            final UUID exportId = UUIDs.timeBased();
                            final ExportRequest exportRequest =
                                    new ExportRequest()
                                            .setElementId(elementId)
                                            .setElementType(elementType)
                                            .setExportId(exportId)
                                            .setRootElementId(rootElementId) // this will set if the export notification is root element export
                                            .setAccountId(accountId)
                                            .setProjectId(workspaceProject.getProjectId())
                                            .setWorkspaceId(workspaceProject.getWorkspaceId())
                                            .setStatus(ExportStatus.IN_PROGRESS)
                                            .setMetadata(metadata);

                            return exportService.submit(exportRequest, exportType)
                                    .collectList()
                                    .doOnEach(log.reactiveDebugSignal("sent courseware export"))
                                    .doOnEach(ReactiveTransaction.linkOnNext())
                                    .map(notifications -> new ExportData()
                                            .setExportId(exportId)
                                            .setElementsExportedCount(notifications.size()));
                        }));
    }
}
