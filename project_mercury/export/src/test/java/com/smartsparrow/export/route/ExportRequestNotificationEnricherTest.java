package com.smartsparrow.export.route;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.annotation.service.CoursewareAnnotation;
import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.ActivityConfig;
import com.smartsparrow.courseware.data.ActivityTheme;
import com.smartsparrow.courseware.data.Component;
import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.Interactive;
import com.smartsparrow.courseware.data.InteractiveConfig;
import com.smartsparrow.courseware.data.ParentByComponent;
import com.smartsparrow.courseware.data.Scenario;
import com.smartsparrow.courseware.payload.ActivityPayload;
import com.smartsparrow.courseware.payload.ComponentPayload;
import com.smartsparrow.courseware.payload.InteractivePayload;
import com.smartsparrow.courseware.payload.PathwayPayload;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.ComponentService;
import com.smartsparrow.courseware.service.InteractiveService;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.courseware.service.ScenarioService;
import com.smartsparrow.courseware.service.WorkspaceAssetService;
import com.smartsparrow.export.data.ExportRequest;
import com.smartsparrow.export.data.ExportRequestNotification;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.math.data.AssetIdByUrn;
import com.smartsparrow.math.service.MathAssetService;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.payload.ExportPluginPayload;
import com.smartsparrow.plugin.service.ExportPluginService;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.workspace.data.ThemePayload;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class ExportRequestNotificationEnricherTest {

    @InjectMocks
    private ExportRequestNotificationEnricher enricher;

    @Mock
    private ActivityService activityService;
    @Mock
    private AnnotationService annotationService;
    @Mock
    private WorkspaceAssetService workspaceAssetService;
    @Mock
    private PathwayService pathwayService;
    @Mock
    private InteractiveService interactiveService;
    @Mock
    private ComponentService componentService;
    @Mock
    private ScenarioService scenarioService;
    @Mock
    private ExportPluginService exportPluginService;
    @Mock
    private MathAssetService mathAssetService;
    //
    private final UUID elementId = UUIDs.timeBased();
    private final UUID rootElementId = UUIDs.timeBased();
    private final String mathUrn = "urn:math:" + elementId;

    private static final UUID pathwayId = UUID.randomUUID();
    private static final UUID pluginId = UUID.randomUUID();
    private static final String pluginVersion = "1.*";
    private static final UUID activityId = com.datastax.driver.core.utils.UUIDs.timeBased();
    private static final UUID interactiveId = UUID.randomUUID();
    private static final UUID componentId = UUID.randomUUID();
    private static final UUID mathAssetId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // default to not found.
        when(activityService.getActivityPayload(any(UUID.class))).thenReturn(Mono.empty());
        when(pathwayService.getPathwayPayload(any(UUID.class))).thenReturn(Mono.empty());
        when(interactiveService.getInteractivePayload(any(UUID.class))).thenReturn(Mono.empty());
        when(componentService.getComponentPayload(any(UUID.class))).thenReturn(Mono.empty());
        when(scenarioService.findScenarios(any(UUID.class))).thenReturn(Flux.empty());

        // add matchers
        ActivityPayload rootActivity = new ActivityPayload();
        when(activityService.getActivityPayload(eq(rootElementId))).thenReturn(Mono.just(rootActivity));

        ActivityPayload activityPayload = new ActivityPayload()
                .setAssets(ImmutableList.of(new AssetPayload().setUrn("assetUrn")));
        when(activityService.getActivityPayload(eq(elementId))).thenReturn(Mono.just(activityPayload));

        //
        when(annotationService.findCoursewareAnnotation(any(UUID.class), any(UUID.class), any(Motivation.class)))
                .thenReturn(Flux.empty());
        when(annotationService.findCoursewareAnnotation(any(UUID.class), eq(elementId), any(Motivation.class)))
                .thenReturn(Flux.just(new CoursewareAnnotation()));
        when(annotationService.findCoursewareAnnotation(isNull(), eq(elementId), any(Motivation.class)))
                .thenReturn(Flux.just(new CoursewareAnnotation()));
        when(annotationService.findCoursewareAnnotation(eq(rootElementId), eq(rootElementId), any(Motivation.class)))
                .thenReturn(Flux.just(new CoursewareAnnotation()));
        when(annotationService.findCoursewareAnnotation(eq(rootElementId), any(UUID.class), any(Motivation.class)))
                .thenReturn(Flux.just(new CoursewareAnnotation()));

        when(exportPluginService.findExportPluginPayload(any(UUID.class), any(String.class))).thenReturn(Mono.empty());
        //
        when(scenarioService.findScenarios(any())).thenReturn(Flux.just(new Scenario()));
        //
        when(workspaceAssetService.getAssetPayload(any(String.class)))
                .thenReturn(Mono.empty());
        when(workspaceAssetService.getAssetPayload(eq("assetUrn")))
                .thenReturn(Mono.just(new AssetPayload()));

        when(mathAssetService.isFeatureEnabled()).thenReturn(true);

        when(mathAssetService.getAssetsFor(any(UUID.class))).thenReturn(Flux.empty());

        when(mathAssetService.getMathAssetPayload(any(UUID.class)))
                .thenReturn(Mono.empty());

        when(pathwayService.getPathwayPayload(eq(elementId)))
                .thenReturn(Mono.just(new PathwayPayload()));
        when(exportPluginService.findExportPluginPayload(any(), any())).thenReturn(Mono.just(new ExportPluginPayload()));
        //
        InteractivePayload interactivePayload = new InteractivePayload()
                .setAssets(ImmutableList.of(new AssetPayload().setUrn("assetUrn")));
        when(interactiveService.getInteractivePayload(eq(elementId)))
                .thenReturn(Mono.just(interactivePayload));
        //
        ComponentPayload componentPayload = new ComponentPayload()
                .setAssets(ImmutableList.of(new AssetPayload().setUrn("assetUrn")));
        when(componentService.getComponentPayload(eq(elementId)))
                .thenReturn(Mono.just(componentPayload));

    }

    @Test
    void handle_nullNotification() {
        // expectation is no error is raised, enricher chain should continue.
        enricher.handle(null);
    }

    @Test
    void handle_noElementType() {
        // expectation is no error is raised, enricher chain should continue.
        ExportRequestNotification notification = new ExportRequestNotification(elementId)
                .setElementType(null);

        enricher.handle(notification);
    }

    @Test
    void handle_noElementId() {
        // expectation is no error is raised, enricher chain should continue.
        ExportRequestNotification notification = new ExportRequestNotification(null)
                .setElementType(CoursewareElementType.ACTIVITY);

        enricher.handle(notification);
    }

    private void mockServicesForActivity() {
        PluginSummary pluginSummary = new PluginSummary().setId(pluginId).setLatestVersion(pluginVersion);
        ActivityPayload expectedActivityPayload = ActivityPayload.from(new Activity().setId(activityId),
                                                                       new ActivityConfig(),
                                                                       pluginSummary,
                                                                       new AccountPayload(),
                                                                       new ActivityTheme(),
                                                                       new ArrayList<>(),
                                                                       new ArrayList<>(),
                                                                       new CoursewareElementDescription(),
                                                                       new ArrayList<>(),
                                                                       new ThemePayload(),
                                                                       Collections.emptyList());
        expectedActivityPayload.setAssets(ImmutableList.of(new AssetPayload().setUrn("assetUrn")));
        when(activityService.getActivityPayload(any(UUID.class))).thenReturn(Mono.just(expectedActivityPayload));
        when(mathAssetService.getMathAssetPayload(eq(elementId))).thenReturn(Mono.just(new AssetPayload().setUrn(mathUrn)));
        when(mathAssetService.getAssetsFor(eq(elementId))).thenReturn(Flux.just(new AssetIdByUrn().setAssetUrn(mathUrn).setAssetId(
                mathAssetId)));
    }

    @Test
    void handle_sub_activity_export() {

        // export request is NOT for root element (LESSON EXPORT, rootElement should exist)
        ExportRequest originalExportRequest = new ExportRequest()
                .setElementId(elementId)
                .setElementType(CoursewareElementType.ACTIVITY)
                .setRootElementId(rootElementId);

        ExportRequestNotification notification = new ExportRequestNotification(elementId,
                                                                               CoursewareElementType.ACTIVITY,
                                                                               originalExportRequest);

        mockServicesForActivity();
        enricher.handle(notification);

        assertNotNull(notification.getActivity());
        assertNotNull(notification.getRootElement());
        assertNotNull(notification.getAnnotations());
        assertNotNull(notification.getResolvedAssets());
        assertNotNull(notification.getScenarios());
        assertNotNull(notification.getPluginPayload());
        assertEquals(2, notification.getResolvedAssets().size());
        verify(mathAssetService, atLeastOnce()).getMathAssetPayload(any(UUID.class));
        verify(mathAssetService, atLeastOnce()).getAssetsFor(eq(elementId));
    }

    @Test
    void handle_activity_notFound() {
        ExportRequestNotification notification = new ExportRequestNotification(UUIDs.timeBased())
                .setElementType(CoursewareElementType.ACTIVITY)
                .setRootElementId(rootElementId);

        enricher.handle(notification);

        assertNull(notification.getActivity());
        assertNull(notification.getRootElement());
        assertNull(notification.getAnnotations());
        assertNull(notification.getResolvedAssets());
    }

    @Test
    void handle_root_activity_export_sub_activity_notification() {

        // export request for root element
        ExportRequest originalExportRequest = new ExportRequest()
                .setElementId(rootElementId)
                .setElementType(CoursewareElementType.ACTIVITY)
                .setRootElementId(rootElementId);

        ExportRequestNotification notification = new ExportRequestNotification(elementId,
                                                                               CoursewareElementType.ACTIVITY,
                                                                               originalExportRequest);

        mockServicesForActivity();
        enricher.handle(notification);

        // root element has to be null in this case, where the export is for the course and the notification is non-root element id
        assertNull(notification.getRootElement());

        assertNotNull(notification.getActivity());
        assertNotNull(notification.getAnnotations());
        assertNotNull(notification.getResolvedAssets());
        assertNotNull(notification.getPluginPayload());
        assertNotNull(notification.getScenarios());
        assertEquals(2, notification.getResolvedAssets().size());
        verify(mathAssetService, atLeastOnce()).getMathAssetPayload(any(UUID.class));
        verify(mathAssetService, atLeastOnce()).getAssetsFor(eq(elementId));
    }


    @Test
    void handle_root_activity_export_root_activity_notification() {

        // export request for root element
        ExportRequest originalExportRequest = new ExportRequest()
                .setElementId(rootElementId)
                .setElementType(CoursewareElementType.ACTIVITY)
                .setRootElementId(rootElementId);

        ExportRequestNotification notification = new ExportRequestNotification(rootElementId,
                                                                               CoursewareElementType.ACTIVITY,
                                                                               originalExportRequest);

        mockServicesForActivity();
        enricher.handle(notification);

        // root element has to be null in this case, where the export is for the course and the notification is root element id\
        assertNull(notification.getRootElement());
        //
        assertNotNull(notification.getActivity());
        assertNotNull(notification.getAnnotations());
        assertNotNull(notification.getResolvedAssets());
        assertNotNull(notification.getPluginPayload());
        assertNotNull(notification.getScenarios());
        assertEquals(notification.getActivity().getAssets().size(), notification.getResolvedAssets().size());
    }


    @Test
    void handle_sub_activity_export_sub_activity_notification() {

        // export request for root element
        ExportRequest originalExportRequest = new ExportRequest()
                .setElementId(elementId)
                .setElementType(CoursewareElementType.ACTIVITY)
                .setRootElementId(rootElementId);

        ExportRequestNotification notification = new ExportRequestNotification(elementId,
                                                                               CoursewareElementType.ACTIVITY,
                                                                               originalExportRequest);

        mockServicesForActivity();
        enricher.handle(notification);

        // root element cannot be null here
        assertNotNull(notification.getRootElement());
        //
        assertNotNull(notification.getActivity());
        assertNotNull(notification.getAnnotations());
        assertNotNull(notification.getResolvedAssets());
        assertNotNull(notification.getPluginPayload());
        assertNotNull(notification.getScenarios());
        assertEquals(2, notification.getResolvedAssets().size());
    }

    @Test
    void handle_sub_activity_export_non_sub_activity_notification() {

        // export request for root element
        ExportRequest originalExportRequest = new ExportRequest()
                .setElementId(elementId)
                .setElementType(CoursewareElementType.ACTIVITY)
                .setRootElementId(rootElementId);

        ExportRequestNotification notification = new ExportRequestNotification(UUID.randomUUID(),
                                                                               CoursewareElementType.ACTIVITY,
                                                                               originalExportRequest);

        mockServicesForActivity();
        enricher.handle(notification);

        // root element has to be null in this case, where the export is for the course and the notification is root element id\
        assertNull(notification.getRootElement());
        //
        assertNotNull(notification.getActivity());
        assertNotNull(notification.getAnnotations());
        assertNotNull(notification.getResolvedAssets());
        assertNotNull(notification.getScenarios());
        assertNotNull(notification.getPluginPayload());
        assertEquals(notification.getActivity().getAssets().size(), notification.getResolvedAssets().size());
    }

    @Test
    void handle_pathway() {
        ExportRequestNotification notification = new ExportRequestNotification(elementId)
                .setElementType(CoursewareElementType.PATHWAY);

        enricher.handle(notification);

        assertNotNull(notification.getPathway());
        assertNull(notification.getRootElement());
        assertNotNull(notification.getAnnotations());
    }

    @Test
    void handle_pathway_notFound() {
        ExportRequestNotification notification = new ExportRequestNotification(UUIDs.timeBased())
                .setElementType(CoursewareElementType.PATHWAY);

        enricher.handle(notification);

        assertNull(notification.getPathway());
    }

    @Test
    void handle_interactive() {
        ExportRequestNotification notification = new ExportRequestNotification(elementId)
                .setElementType(CoursewareElementType.INTERACTIVE)
                .setRootElementId(rootElementId);

        Interactive newInteractive = new Interactive().setId(interactiveId);
        PluginSummary pluginSummary = new PluginSummary().setId(pluginId).setLatestVersion(pluginVersion);
        InteractivePayload expectedInteractivePayload = InteractivePayload.from(newInteractive,
                                                                                pluginSummary,
                                                                                new InteractiveConfig(),
                                                                                pathwayId,
                                                                                new ArrayList<>(),
                                                                                new ArrayList<>(),
                                                                                new CoursewareElementDescription(),
                                                                                Lists.newArrayList());
        expectedInteractivePayload.setAssets(ImmutableList.of(new AssetPayload().setUrn("assetUrn")));
        when(interactiveService.getInteractivePayload(any(UUID.class))).thenReturn(Mono.just(expectedInteractivePayload));
        when(mathAssetService.getMathAssetPayload(eq(elementId))).thenReturn(Mono.just(new AssetPayload().setUrn(mathUrn)));
        when(mathAssetService.getAssetsFor(eq(elementId))).thenReturn(Flux.just(new AssetIdByUrn().setAssetUrn(mathUrn).setAssetId(
                mathAssetId)));

        enricher.handle(notification);

        assertNotNull(notification.getInteractive());
        assertNotNull(notification.getRootElement());
        assertNotNull(notification.getAnnotations());
        assertNotNull(notification.getResolvedAssets());
        assertNotNull(notification.getPluginPayload());
        assertNotNull(notification.getScenarios());
        assertEquals(2, notification.getResolvedAssets().size());
        verify(mathAssetService, atLeastOnce()).getMathAssetPayload(any(UUID.class));
        verify(mathAssetService, atLeastOnce()).getAssetsFor(eq(elementId));
    }

    @Test
    void handle_interactive_notFound() {
        ExportRequestNotification notification = new ExportRequestNotification(UUIDs.timeBased())
                .setElementType(CoursewareElementType.INTERACTIVE);

        enricher.handle(notification);

        assertNull(notification.getInteractive());
    }

    @Test
    void handle_component() {
        ExportRequestNotification notification = new ExportRequestNotification(elementId)
                .setElementType(CoursewareElementType.COMPONENT);

        Component component = new Component().setId(componentId);
        PluginSummary pluginSummary = new PluginSummary().setId(pluginId).setLatestVersion(pluginVersion);
        ComponentPayload componentPayload = ComponentPayload.from(component,
                                                                  "some config",
                                                                  pluginSummary,
                                                                  new ParentByComponent(),
                                                                  new CoursewareElementDescription(),
                                                                  new ArrayList<>());
        componentPayload.setAssets(ImmutableList.of(new AssetPayload().setUrn("assetUrn")));
        when(componentService.getComponentPayload(any(UUID.class))).thenReturn(Mono.just(componentPayload));
        enricher.handle(notification);

        assertNotNull(notification.getComponent());
        assertNotNull(notification.getAnnotations());
        assertNotNull(notification.getPluginPayload());
        assertNotNull(notification.getResolvedAssets());
        assertEquals(1, notification.getResolvedAssets().size());
    }

    @Test
    void handle_component_notFound() {
        ExportRequestNotification notification = new ExportRequestNotification(UUIDs.timeBased())
                .setElementType(CoursewareElementType.COMPONENT);

        enricher.handle(notification);

        assertNull(notification.getComponent());
        assertNull(notification.getAnnotations());
        assertNull(notification.getResolvedAssets());
    }

    @Test
    void handle_activity_math_feature_not_enabled() {
        ExportRequestNotification notification = new ExportRequestNotification(elementId)
                .setElementType(CoursewareElementType.ACTIVITY)
                .setRootElementId(rootElementId);
        
        PluginSummary pluginSummary = new PluginSummary().setId(pluginId).setLatestVersion(pluginVersion);
        ActivityPayload expectedActivityPayload = ActivityPayload.from(new Activity().setId(activityId),
                                                                       new ActivityConfig(),
                                                                       pluginSummary,
                                                                       new AccountPayload(),
                                                                       new ActivityTheme(),
                                                                       new ArrayList<>(),
                                                                       new ArrayList<>(),
                                                                       new CoursewareElementDescription(),
                                                                       new ArrayList<>(),
                                                                       new ThemePayload(),
                                                                       Collections.emptyList());
        expectedActivityPayload.setAssets(ImmutableList.of(new AssetPayload().setUrn("assetUrn")));
        when(activityService.getActivityPayload(any(UUID.class))).thenReturn(Mono.just(expectedActivityPayload));
        when(mathAssetService.isFeatureEnabled()).thenReturn(false);

        enricher.handle(notification);

        assertNotNull(notification.getActivity());
        assertNotNull(notification.getRootElement());
        assertNotNull(notification.getAnnotations());
        assertNotNull(notification.getResolvedAssets());
        assertNotNull(notification.getPluginPayload());
        assertNotNull(notification.getScenarios());
        assertEquals(1, notification.getResolvedAssets().size());
        verify(mathAssetService, never()).getMathAssetPayload(eq(elementId));
        verify(mathAssetService, never()).getAssetsFor(eq(elementId));
    }
}
