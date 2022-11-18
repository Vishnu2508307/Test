package com.smartsparrow.export.route;

import static com.smartsparrow.annotation.service.Motivation.describing;
import static com.smartsparrow.annotation.service.Motivation.identifying;
import static com.smartsparrow.annotation.service.Motivation.linking;
import static com.smartsparrow.annotation.service.Motivation.tagging;
import static com.smartsparrow.courseware.data.CoursewareElementType.ACTIVITY;
import static com.smartsparrow.courseware.data.CoursewareElementType.INTERACTIVE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.apache.commons.collections4.ListUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.newrelic.api.agent.Trace;
import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.annotation.service.CoursewareAnnotation;
import com.smartsparrow.annotation.service.Motivation;
import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.payload.ActivityPayload;
import com.smartsparrow.courseware.payload.ComponentPayload;
import com.smartsparrow.courseware.payload.InteractivePayload;
import com.smartsparrow.courseware.payload.PathwayPayload;
import com.smartsparrow.courseware.payload.ScenarioPayload;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.courseware.service.ComponentService;
import com.smartsparrow.courseware.service.InteractiveService;
import com.smartsparrow.courseware.service.PathwayService;
import com.smartsparrow.courseware.service.ScenarioService;
import com.smartsparrow.courseware.service.WorkspaceAssetService;
import com.smartsparrow.export.data.ExportRequestNotification;
import com.smartsparrow.math.data.AssetIdByUrn;
import com.smartsparrow.math.service.MathAssetService;
import com.smartsparrow.plugin.payload.PluginRefPayload;
import com.smartsparrow.plugin.service.ExportPluginService;
import com.smartsparrow.util.log.MercuryLogger;
import com.smartsparrow.util.log.MercuryLoggerFactory;

/**
 * Enrich an ExportRequestNotification with additional data in these fields:
 * <pre>
 * {
 *   "activity": ActivityDTO,
 *   "pathway": PathwayDTO,
 *   "interactive": InteractiveDTO,
 *   "component": ComponentDTO,
 *   "annotations": [CoursewareAnnotation, ...],
 *   "assets": [AssetDTO, ...]
 * }
 * </pre>
 */
public class ExportRequestNotificationEnricher {
    private static final MercuryLogger log = MercuryLoggerFactory.getLogger(ExportRequestNotificationEnricher.class);

    private final ActivityService activityService;
    private final AnnotationService annotationService;
    private final WorkspaceAssetService workspaceAssetService;
    private final PathwayService pathwayService;
    private final InteractiveService interactiveService;
    private final ComponentService componentService;
    private final ScenarioService scenarioService;
    private final ExportPluginService exportPluginService;
    private final MathAssetService mathAssetService;

    @Inject
    public ExportRequestNotificationEnricher(final ActivityService activityService,
                                             final AnnotationService annotationService,
                                             final WorkspaceAssetService workspaceAssetService,
                                             final PathwayService pathwayService,
                                             final InteractiveService interactiveService,
                                             final ComponentService componentService,
                                             final ScenarioService scenarioService,
                                             final ExportPluginService exportPluginService,
                                             final MathAssetService mathAssetService) {
        this.activityService = activityService;
        this.annotationService = annotationService;
        this.workspaceAssetService = workspaceAssetService;
        this.pathwayService = pathwayService;
        this.interactiveService = interactiveService;
        this.componentService = componentService;
        this.scenarioService = scenarioService;
        this.exportPluginService = exportPluginService;
        this.mathAssetService = mathAssetService;
    }

    @Handler
    @Trace(dispatcher = true, metricName = "export-request-notification-enrich")
    public void handle(@Body ExportRequestNotification notification) {
        // do nothing if the notification is null
        if (notification == null) {
            log.jsonError("Unable to enrich ExportRequestNotification.", new HashMap<>(), new Exception());
            return;
        }
        // primary error cases to trap and log.
        if (notification.getElementType() == null || notification.getElementId() == null) {
            log.jsonWarn("Unable to enrich ExportRequestNotification.",
                         new HashMap<String, Object>() {
                             {
                                 put("notification", notification);
                             }
                         });
            return;
        }

        //
        // fill in the type specific data fields.
        switch (notification.getElementType()) {
            case ACTIVITY:
                //
                ActivityPayload activity = activityService.getActivityPayload(notification.getElementId()).block();
                if (activity == null) {
                    log.jsonWarn("Unable to enrich ExportRequestNotification; activity not found.",
                                 ImmutableMap.of("elementType", notification.getElementType(),
                                                 "elementId", notification.getElementId()));
                    return;
                }
                notification.setActivity(activity);
                //
                setRootActivity(notification.getRootElementId(), notification);
                setAnnotations(notification.getRootElementId(), notification.getElementId(), notification);
                setResolvedAssets(activity.getAssets(), notification);
                setScenarios(activity.getActivityId(), ACTIVITY, notification);
                setPluginPayload(activity.getPlugin(), notification);
                break;

            case PATHWAY:
                //
                PathwayPayload pathway = pathwayService.getPathwayPayload(notification.getElementId()).block();
                if (pathway == null) {
                    log.jsonWarn("Unable to enrich ExportRequestNotification; pathway not found.",
                                 ImmutableMap.of("elementType", notification.getElementType(),
                                                 "elementId", notification.getElementId()));
                    return;
                }
                notification.setPathway(pathway);
                //
                setAnnotations(notification.getRootElementId(), notification.getElementId(), notification);
                setResolvedAssets(pathway.getAssets(), notification);
                break;

            case INTERACTIVE:
                //
                InteractivePayload interactive = interactiveService.getInteractivePayload(notification.getElementId()).block();
                if (interactive == null) {
                    log.jsonWarn("Unable to enrich ExportRequestNotification; interactive not found.",
                                 ImmutableMap.of("elementType", notification.getElementType(),
                                                 "elementId", notification.getElementId()));
                    return;
                }
                notification.setInteractive(interactive);
                //
                setRootActivity(notification.getRootElementId(), notification);
                setAnnotations(notification.getRootElementId(), notification.getElementId(), notification);
                setResolvedAssets(interactive.getAssets(), notification);
                setScenarios(interactive.getInteractiveId(), INTERACTIVE, notification);
                setPluginPayload(interactive.getPlugin(), notification);
                break;

            case COMPONENT:
                //
                ComponentPayload component = componentService.getComponentPayload(notification.getElementId()).block();
                if (component == null) {
                    log.jsonWarn("Unable to enrich ExportRequestNotification; component not found.",
                                 ImmutableMap.of("elementType", notification.getElementType(),
                                                 "elementId", notification.getElementId()));
                    return;
                }
                notification.setComponent(component);
                //
                setAnnotations(notification.getRootElementId(), notification.getElementId(), notification);
                setResolvedAssets(component.getAssets(), notification);
                setPluginPayload(component.getPlugin(), notification);
                break;
            default:
                // nothing to do here
                break;
        }
    }

    /*
     * Set the root activity into the notification.
     */
    void setRootActivity(final UUID rootElementId, ExportRequestNotification notification) {
        if (rootElementId == null
                || notification.isRootElementExport() // if the root elementId is null or if the notification is a root element export, then do not attach root element to the notification
                || notification.getElementId() != notification.getExportElementId()) { // if it's not course export and
            return;
        }

        ActivityPayload rootElement = activityService.getActivityPayload(rootElementId).block();
        notification.setRootElement(rootElement);
    }

    /*
     * Resolve and set the assets (with URLs) into the notification
     *
     * Will not set the field in the notification if the supplied assets are null or empty (or no assets are resolved)
     */
    void setResolvedAssets(final List<AssetPayload> assets, ExportRequestNotification notification) {
        List<AssetIdByUrn> mathAssets = null;
        if (mathAssetService.isFeatureEnabled()) {
           mathAssets = mathAssetService.getAssetsFor(notification.getElementId()).collectList().block();
        }

        if (assets == null && mathAssets == null) {
            return;
        }

        // FIXME: assets are included in the activity|interactive|etc., but need a deeper API call to get the actual URL.
        // FIXME: this MUST be refactored out later to a streamlined fetch in the activity load.
        List<AssetPayload> resolvedAssets = new ArrayList<>();

        for (AssetPayload asset : ListUtils.emptyIfNull(assets)) {
            String urn = asset.getUrn();
            AssetPayload block = workspaceAssetService.getAssetPayload(urn).block();
            resolvedAssets.add(block);
        }

        for (AssetIdByUrn mathAsset : ListUtils.emptyIfNull(mathAssets)) {
            UUID assetId = mathAsset.getAssetId();
            AssetPayload block = mathAssetService.getMathAssetPayload(assetId).block();
            resolvedAssets.add(block);
        }

        if (!resolvedAssets.isEmpty()) {
            notification.setResolvedAssets(resolvedAssets);
        }
    }

    /*
     * Fetch and set the annotations into the notification
     *
     * Will not set the field in the notification if no annotations exist.
     */
    void setAnnotations(final UUID rootElementId, final UUID elementId, ExportRequestNotification notification) {
        // find all the annotations; collect to a list in the notification.
        List<CoursewareAnnotation> annotations = new ArrayList<>();
        for (Motivation motivation : new Motivation[]{tagging, linking, identifying, describing}) {
            List<CoursewareAnnotation> list = annotationService.findCoursewareAnnotation(rootElementId,
                                                                                         elementId,
                                                                                         motivation)
                    .collectList().block();
            // collect all the annotations.
            if (list != null) {
                annotations.addAll(list);
            }
        }

        if (!annotations.isEmpty()) {
            notification.setAnnotations(annotations);
        }
    }

    /*
     * Fetch and set the scenarios into the notification
     */
    void setScenarios(final UUID parentId, final CoursewareElementType parentType, ExportRequestNotification notification) {
        // find all the scenarios; collect to a list in the notification.
        List<ScenarioPayload> scenarios = scenarioService.findScenarios(parentId)
                .map(scenario -> ScenarioPayload.from(parentId, parentType, scenario))
                .collectList().defaultIfEmpty(Lists.newArrayList()).block();

        if (scenarios != null && !scenarios.isEmpty()) {
            notification.setScenarios(scenarios);
        }
    }

    /*
     * Add plugin summary and repository path to notification
     */
    void setPluginPayload(final PluginRefPayload plugin, ExportRequestNotification notification) {
        notification.setPluginPayload(exportPluginService.findExportPluginPayload(plugin.getPluginId(), plugin.getVersionExpr()).block());
    }
}
