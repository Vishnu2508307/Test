package com.smartsparrow.courseware.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;
import com.smartsparrow.asset.data.Asset;
import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.Component;
import com.smartsparrow.courseware.data.ComponentConfig;
import com.smartsparrow.courseware.data.ComponentGateway;
import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.Interactive;
import com.smartsparrow.courseware.data.ManualGradingConfiguration;
import com.smartsparrow.courseware.data.ManualGradingConfigurationGateway;
import com.smartsparrow.courseware.data.ParentByComponent;
import com.smartsparrow.courseware.lang.ActivityNotFoundException;
import com.smartsparrow.courseware.lang.ComponentAlreadyExistsFault;
import com.smartsparrow.courseware.lang.ComponentNotFoundException;
import com.smartsparrow.courseware.lang.ComponentParentNotFound;
import com.smartsparrow.courseware.lang.InteractiveNotFoundException;
import com.smartsparrow.courseware.payload.ComponentPayload;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.plugin.data.PluginFilter;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.data.PluginType;
import com.smartsparrow.plugin.lang.VersionParserFault;
import com.smartsparrow.plugin.service.PluginService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

public class ComponentServiceTest {

    private static final UUID interactiveId = UUID.randomUUID();
    private static final UUID activityId = UUID.randomUUID();
    private static final UUID pluginId = UUID.randomUUID();
    private static final String pluginVersionExpr = "v1.2.4";
    private static final UUID componentId = UUID.randomUUID();
    private static final String config = "foo";
    private static final UUID rootElementId = UUID.randomUUID();
    @InjectMocks
    ComponentService componentService;
    @Mock
    ComponentGateway componentGateway;
    @Mock
    PluginService pluginService;
    @Mock
    InteractiveService interactiveService;
    @Mock
    private ActivityService activityService;
    @Mock
    private CoursewareAssetService coursewareAssetService;
    @Mock
    private CoursewareElementDescriptionService descriptionService;
    @Mock
    private ManualGradingConfigurationGateway manualGradingConfigurationGateway;
    @Mock
    private Asset asset;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(manualGradingConfigurationGateway.find(componentId)).thenReturn(Mono.empty());
        when(descriptionService.fetchCoursewareDescriptionByElement(componentId))
                .thenReturn(Mono.just(new CoursewareElementDescription(componentId, CoursewareElementType.COMPONENT, "Component Description")));
        when(asset.getAssetMediaType()).thenReturn(AssetMediaType.IMAGE);
        List<PluginFilter> pluginFilterList = new ArrayList<>();
        when(pluginService.fetchPluginFiltersByIdVersionExpr(any(), any())).thenReturn(Mono.just(pluginFilterList));
    }

    @Test
    void createComponent_nullInteractiveId() {
        Throwable e = assertThrows(IllegalArgumentException.class,
                () -> componentService.createForInteractive(null, null, null, null, null));
        assertEquals(e.getMessage(), "missing interactive id");
    }

    @Test
    void createComponent_nullPluginId() {

        Throwable e = assertThrows(IllegalArgumentException.class,
                () -> componentService.createForInteractive(interactiveId, null, null, null, null));
        assertEquals(e.getMessage(), "missing plugin id");
    }

    @Test
    void createComponent_nullPluginVersionExpr() {

        Throwable e = assertThrows(IllegalArgumentException.class,
                () -> componentService.createForInteractive(interactiveId, pluginId, null, null, null));
        assertEquals(e.getMessage(), "missing plugin version");
    }

    @Test
    void createComponentValidWithNullConfig() throws VersionParserFault {
        Component component = new Component()
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersionExpr);

        when(pluginService.findLatestVersion(any(UUID.class), any(String.class))).thenReturn(Mono.just(pluginVersionExpr));
        when(componentGateway.persist(any(Component.class), eq(interactiveId), eq(CoursewareElementType.INTERACTIVE)))
                .thenReturn(Mono.empty());
        when(interactiveService.findById(eq(interactiveId))).thenReturn(Mono.empty());

        Component result = componentService.createForInteractive(interactiveId, pluginId, pluginVersionExpr, null)
                .block();

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(component.getPluginId(), result.getPluginId());
        assertEquals(component.getPluginVersionExpr(), result.getPluginVersionExpr());
    }

    @Test
    void createComponentValidWithConfig() throws VersionParserFault {
        Component component = new Component()
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersionExpr);

        when(pluginService.findLatestVersion(any(UUID.class), any(String.class))).thenReturn(Mono.just(pluginVersionExpr));
        when(componentGateway.persist(any(Component.class), eq(interactiveId), eq(CoursewareElementType.INTERACTIVE)))
                .thenReturn(Mono.empty());
        when(interactiveService.findById(eq(interactiveId))).thenReturn(Mono.empty());

        when(componentGateway.persist(any(ComponentConfig.class))).thenReturn(Mono.empty());

        Component result = componentService.createForInteractive(interactiveId, pluginId, pluginVersionExpr, config)
                .block();

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(component.getPluginId(), result.getPluginId());
        assertEquals(component.getPluginVersionExpr(), result.getPluginVersionExpr());
    }

    @SuppressWarnings("unchecked")
    @Test
    void createComponent_interactiveNotFound() throws VersionParserFault {
        when(pluginService.findLatestVersion(any(UUID.class), any(String.class))).thenReturn(Mono.just(pluginVersionExpr));
        when(componentGateway.persist(any(Component.class), eq(interactiveId), eq(CoursewareElementType.INTERACTIVE)))
                .thenReturn(Mono.empty());
        Mono mono = TestPublisher.create().error(new InteractiveNotFoundException(interactiveId)).mono();
        when(interactiveService.findById(eq(interactiveId))).thenReturn(mono);

        assertThrows(InteractiveNotFoundException.class,
                () -> componentService.createForInteractive(interactiveId, pluginId, pluginVersionExpr, null).block());
    }

    @Test
    void createInteractiveComponentValidWithComponentId() throws VersionParserFault {
        Component component = new Component()
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersionExpr);

        when(componentGateway.findById(any(UUID.class))).thenReturn(Mono.empty());
        when(pluginService.findLatestVersion(any(UUID.class), any(String.class))).thenReturn(Mono.just(pluginVersionExpr));
        when(componentGateway.persist(any(Component.class), eq(interactiveId), eq(CoursewareElementType.INTERACTIVE)))
                .thenReturn(Mono.empty());
        when(interactiveService.findById(eq(interactiveId))).thenReturn(Mono.empty());

        Component result = componentService.createForInteractive(interactiveId, pluginId, pluginVersionExpr, null, componentId)
                .block();

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(component.getPluginId(), result.getPluginId());
        assertEquals(component.getPluginVersionExpr(), result.getPluginVersionExpr());
    }

    @Test
    void createInteractiveComponentWithComponentIdConflict() {
        Component component = new Component()
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersionExpr);

        when(componentGateway.findById(componentId)).thenReturn(Mono.just(component));

        assertThrows(ComponentAlreadyExistsFault.class,
                     () -> componentService.createForInteractive(interactiveId,
                                                              pluginId,
                                                              pluginVersionExpr,
                                                              null,
                                                              componentId).block());

        verify(componentGateway).findById(componentId);
        verify(pluginService, never()).findLatestVersion(any(UUID.class), any(String.class));
        verify(interactiveService, never()).findById(eq(interactiveId));
        verify(componentGateway, never()).persist(any(Component.class),
                                                  eq(interactiveId),
                                                  eq(CoursewareElementType.INTERACTIVE));
    }

    @Test
    void createActivityComponent_nullActivityId() {
        Throwable e = assertThrows(IllegalArgumentException.class,
                () -> componentService.createForActivity(null, null, null, null, null));
        assertEquals(e.getMessage(), "missing activity id");
    }

    @Test
    void createActivityComponent_nullPluginId() {

        Throwable e = assertThrows(IllegalArgumentException.class,
                () -> componentService.createForActivity(activityId, null, null, null, null));
        assertEquals(e.getMessage(), "missing plugin id");
    }

    @Test
    void createActivityComponent_nullPluginVersionExpr() {

        Throwable e = assertThrows(IllegalArgumentException.class,
                () -> componentService.createForInteractive(activityId, pluginId, null, null, null));
        assertEquals(e.getMessage(), "missing plugin version");
    }

    @Test
    void createActivityComponentValidWithComponentId() throws VersionParserFault {
        Component component = new Component()
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersionExpr);

        when(componentGateway.findById(any(UUID.class))).thenReturn(Mono.empty());
        when(pluginService.findLatestVersion(any(UUID.class), any(String.class))).thenReturn(Mono.just(pluginVersionExpr));
        when(componentGateway.persist(any(Component.class), eq(activityId), eq(CoursewareElementType.ACTIVITY)))
                .thenReturn(Mono.empty());
        when(activityService.findById(eq(activityId))).thenReturn(Mono.empty());

        Component result = componentService.createForActivity(activityId, pluginId, pluginVersionExpr, null, componentId)
                .block();

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(component.getPluginId(), result.getPluginId());
        assertEquals(component.getPluginVersionExpr(), result.getPluginVersionExpr());
    }

    @Test
    void createActivityComponentWithComponentIdConflict() {
        Component component = new Component()
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersionExpr);

        when(componentGateway.findById(componentId)).thenReturn(Mono.just(component));

        assertThrows(ComponentAlreadyExistsFault.class,
                     () -> componentService.createForActivity(activityId,
                                                              pluginId,
                                                              pluginVersionExpr,
                                                              null,
                                                              componentId).block());

        verify(componentGateway).findById(componentId);
        verify(pluginService, never()).findLatestVersion(any(UUID.class), any(String.class));
        verify(activityService, never()).findById(eq(activityId));
        verify(componentGateway, never()).persist(any(Component.class),
                                                  eq(activityId),
                                                  eq(CoursewareElementType.ACTIVITY));
    }

    @Test
    void createActivityComponentValidWithNullConfig() throws VersionParserFault {
        Component component = new Component()
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersionExpr);

        when(pluginService.findLatestVersion(any(UUID.class), any(String.class))).thenReturn(Mono.just(pluginVersionExpr));
        when(componentGateway.persist(any(Component.class), eq(activityId), eq(CoursewareElementType.ACTIVITY)))
                .thenReturn(Mono.empty());
        when(activityService.findById(eq(activityId))).thenReturn(Mono.empty());

        Component result = componentService.createForActivity(activityId, pluginId, pluginVersionExpr, null)
                .block();

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(component.getPluginId(), result.getPluginId());
        assertEquals(component.getPluginVersionExpr(), result.getPluginVersionExpr());
    }

    @Test
    void createActivityComponentValidWithConfig() throws VersionParserFault {
        Component component = new Component()
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersionExpr);

        when(pluginService.findLatestVersion(any(UUID.class), any(String.class))).thenReturn(Mono.just(pluginVersionExpr));
        when(componentGateway.persist(any(Component.class), eq(activityId), eq(CoursewareElementType.ACTIVITY)))
                .thenReturn(Mono.empty());
        when(componentGateway.persist(any(ComponentConfig.class))).thenReturn(Mono.empty());
        when(activityService.findById(eq(activityId))).thenReturn(Mono.empty());

        Component result = componentService.createForActivity(activityId, pluginId, pluginVersionExpr, config)
                .block();

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(component.getPluginId(), result.getPluginId());
        assertEquals(component.getPluginVersionExpr(), result.getPluginVersionExpr());
    }

    @SuppressWarnings("unchecked")
    @Test
    void createActivityComponent_ActivityNotFound() throws VersionParserFault {
        when(pluginService.findLatestVersion(any(UUID.class), any(String.class))).thenReturn(Mono.just(pluginVersionExpr));
        when(componentGateway.persist(any(Component.class), eq(activityId), eq(CoursewareElementType.ACTIVITY)))
                .thenReturn(Mono.empty());
        Mono mono = TestPublisher.create().error(new ActivityNotFoundException(activityId)).mono();
        when(activityService.findById(eq(activityId))).thenReturn(mono);

        assertThrows(ActivityNotFoundException.class,
                () -> componentService.createForActivity(activityId, pluginId, pluginVersionExpr, null).block());
    }

    @Test
    void replaceComponentConfig_nullComponentId() {
        Throwable e = assertThrows(IllegalArgumentException.class,
                () -> componentService.replaceConfig(null, null));

        assertEquals(e.getMessage(), "missing component id");
    }

    @Test
    void replaceComponentConfig_nullConfig() {
        Throwable e = assertThrows(IllegalArgumentException.class,
                () -> componentService.replaceConfig(componentId, null));

        assertEquals(e.getMessage(), "missing config");
    }

    @Test
    void replaceConfig_valid() {
        ComponentConfig componentConfig = new ComponentConfig()
                .setComponentId(componentId)
                .setConfig(config);

        when(componentGateway.persist(any(ComponentConfig.class))).thenReturn(Mono.empty());
        when(componentGateway.findById(any(UUID.class))).thenReturn(Mono.just(new Component()));

        ComponentConfig result = componentService.replaceConfig(componentId, config).block();

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(componentConfig.getComponentId(), result.getComponentId());
        assertEquals(componentConfig.getConfig(), result.getConfig());
    }

    @Test
    void replaceConfig_componentNotFound() {
        when(componentGateway.persist(any(ComponentConfig.class))).thenReturn(Mono.empty());
        when(componentGateway.findById(any())).thenReturn(Mono.empty());

        assertThrows(ComponentNotFoundException.class, () -> componentService.replaceConfig(componentId, config).block());
    }

    @Test
    void findParentFor_notFound() {

        when(componentGateway.findParentBy(componentId)).thenReturn(Mono.empty());

        StepVerifier.create(componentService.findParentFor(componentId))
                .expectError(ComponentParentNotFound.class).verify();
    }

    @Test
    void getComponentPayload_componentNotFound() {
        when(componentGateway.findById(componentId)).thenReturn(Mono.empty());
        when(componentGateway.findLatestConfig(componentId)).thenReturn(Mono.empty());
        when(componentGateway.findParentBy(componentId)).thenReturn(Mono.empty());

        StepVerifier.create(componentService.getComponentPayload(componentId))
                .expectError(ComponentNotFoundException.class).verify();
    }

    @Test
    void getComponentPayload_parentNotFound() {
        Component component = new Component()
                .setId(componentId)
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersionExpr);

        when(componentGateway.findById(componentId)).thenReturn(Mono.just(component));
        when(componentGateway.findLatestConfig(componentId)).thenReturn(Mono.empty());
        when(componentGateway.findParentBy(componentId)).thenReturn(Mono.empty());
        when(pluginService.find(pluginId)).thenReturn(Mono.empty());

        StepVerifier.create(componentService.getComponentPayload(componentId))
                .expectError(ComponentParentNotFound.class).verify();
    }

    @Test
    void getComponentPayload_configNotFound_noAsset() {
        UUID parentId = UUID.randomUUID();
        Component component = new Component()
                .setId(componentId)
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersionExpr);

        ParentByComponent parent = new ParentByComponent()
                .setParentId(parentId)
                .setParentType(CoursewareElementType.ACTIVITY)
                .setComponentId(componentId);

        PluginSummary plugin = new PluginSummary()
                .setName("plugin name")
                .setId(pluginId)
                .setType(PluginType.LESSON);

        when(componentGateway.findById(componentId)).thenReturn(Mono.just(component));
        when(componentGateway.findLatestConfig(componentId)).thenReturn(Mono.empty());
        when(componentGateway.findParentBy(componentId)).thenReturn(Mono.just(parent));
        when(pluginService.find(pluginId)).thenReturn(Mono.just(plugin));
        when(coursewareAssetService.getAssetPayloads(componentId)).thenReturn(Mono.empty());

        ComponentPayload payload = componentService.getComponentPayload(componentId).block();

        assertNotNull(payload);
        assertEquals("", payload.getConfig());
        assertEquals(componentId, payload.getComponentId());
        assertEquals(parent.getParentId(), payload.getParentId());
        assertEquals(parent.getParentType(), payload.getParentType());
        assertEquals("Component Description", payload.getDescription());

        assertNotNull(payload.getPlugin());
        assertNull(payload.getAssets());
    }

    @Test
    void getComponentPayload_everythingIsFound() {
        ComponentConfig config = new ComponentConfig()
                .setConfig("to the moon and back");
        UUID parentId = UUID.randomUUID();
        Component component = new Component()
                .setId(componentId)
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersionExpr);

        ParentByComponent parent = new ParentByComponent()
                .setParentId(parentId)
                .setParentType(CoursewareElementType.ACTIVITY)
                .setComponentId(componentId);

        PluginSummary plugin = new PluginSummary()
                .setName("plugin name")
                .setId(pluginId)
                .setType(PluginType.LESSON);

        when(componentGateway.findById(componentId)).thenReturn(Mono.just(component));
        when(componentGateway.findLatestConfig(componentId)).thenReturn(Mono.just(config));
        when(componentGateway.findParentBy(componentId)).thenReturn(Mono.just(parent));
        when(pluginService.find(pluginId)).thenReturn(Mono.just(plugin));
        Map<String, String> data = new HashMap<>();
        data.put("url", "http://url");
        AssetPayload assetPayload = new AssetPayload()
                .setUrn("urn1")
                .setAsset(asset)
                .putSource("original", data);
        when(coursewareAssetService.getAssetPayloads(componentId)).thenReturn(Mono.just(Lists.newArrayList(assetPayload)));

        ComponentPayload payload = componentService.getComponentPayload(componentId).block();

        assertNotNull(payload);
        assertEquals("to the moon and back", payload.getConfig());
        assertEquals(componentId, payload.getComponentId());
        assertEquals(parent.getParentId(), payload.getParentId());
        assertEquals(parent.getParentType(), payload.getParentType());
        assertNotNull(payload.getPlugin());
        assertEquals(1, payload.getAssets().size());
        assertEquals(assetPayload, payload.getAssets().get(0));
        assertEquals("Component Description", payload.getDescription());
    }

    @Test
    void duplicate_componentNotFound() {
        when(componentGateway.findById(componentId)).thenReturn(Mono.empty());

        assertThrows(ComponentNotFoundException.class,
                () -> componentService.duplicate(componentId, UUID.randomUUID(), null, new DuplicationContext()).block());
    }

    @Test
    void duplicate_invalidCoursewareElementType() {
        Component component = new Component()
                .setId(componentId);
        when(componentGateway.findById(componentId)).thenReturn(Mono.just(component));

        assertThrows(UnsupportedOperationException.class,
                () -> componentService.duplicate(componentId, UUID.randomUUID(), CoursewareElementType.PATHWAY, new DuplicationContext()).block());
    }

    @Test
    void duplicate_activityComponentNoConfig() {
        TestPublisher<Void> persisted = TestPublisher.create();
        persisted.complete();
        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);

        Component component = new Component()
                .setId(componentId)
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersionExpr);

        UUID activityId = UUID.randomUUID();

        when(componentGateway.findById(componentId)).thenReturn(Mono.just(component));
        when(componentGateway.findLatestConfig(componentId)).thenReturn(Mono.empty());
        when(activityService.findById(activityId)).thenReturn(Mono.just(new Activity()));
        when(componentGateway.persist(any(Component.class), eq(activityId), eq(CoursewareElementType.ACTIVITY)))
                .thenReturn(persisted.mono());
        when(pluginService.findLatestVersion(pluginId, pluginVersionExpr)).thenReturn(Mono.just("1.0"));
        when(coursewareAssetService.duplicateAssets(eq(componentId), any(), eq(CoursewareElementType.COMPONENT), any(DuplicationContext.class))).thenReturn(Flux.empty());

        Component copied = componentService.duplicate(componentId, activityId, CoursewareElementType.ACTIVITY, new DuplicationContext()).block();

        assertNotNull(copied);
        assertEquals(pluginId, copied.getPluginId());
        assertEquals(pluginVersionExpr, copied.getPluginVersionExpr());
        assertNotEquals(componentId, copied.getId());

        verify(componentGateway, atLeastOnce()).persist(captor.capture(), eq(activityId), eq(CoursewareElementType.ACTIVITY));
        assertEquals(captor.getValue().getId(), copied.getId());
        verify(componentGateway, never()).persist(any(ComponentConfig.class));
    }

    @Test
    void duplicate_activityComponent() {
        TestPublisher<Void> persisted = TestPublisher.create();
        persisted.complete();
        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);

        Component component = new Component()
                .setId(componentId)
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersionExpr);

        ComponentConfig componentConfig = new ComponentConfig()
                .setComponentId(componentId)
                .setConfig(config)
                .setId(UUID.randomUUID());

        UUID activityId = UUID.randomUUID();
        DuplicationContext duplicationContext = new DuplicationContext()
                .setNewRootElementId(rootElementId);

        when(componentGateway.findById(any())).thenReturn(Mono.just(component));
        when(componentGateway.findLatestConfig(componentId)).thenReturn(Mono.just(componentConfig));
        when(activityService.findById(activityId)).thenReturn(Mono.just(new Activity()));
        when(componentGateway.persist(any(Component.class), eq(activityId), eq(CoursewareElementType.ACTIVITY)))
                .thenReturn(persisted.mono());
        when(componentGateway.persist(any(ComponentConfig.class))).thenReturn(persisted.mono());
        when(pluginService.findLatestVersion(pluginId, pluginVersionExpr)).thenReturn(Mono.just("1.0"));
        when(coursewareAssetService.duplicateAssets(eq(componentId), any(), eq(CoursewareElementType.COMPONENT), any(DuplicationContext.class))).thenReturn(Flux.empty());

        Component copied = componentService.duplicate(componentId, activityId, CoursewareElementType.ACTIVITY, duplicationContext).block();

        assertNotNull(copied);
        assertEquals(pluginId, copied.getPluginId());
        assertEquals(pluginVersionExpr, copied.getPluginVersionExpr());
        assertNotEquals(componentId, copied.getId());

        verify(componentGateway).persist(captor.capture(), eq(activityId), eq(CoursewareElementType.ACTIVITY));
        assertEquals(captor.getValue().getId(), copied.getId());
        verify(componentGateway).persist(any(ComponentConfig.class));
        verify(coursewareAssetService).duplicateAssets(componentId, copied.getId(), CoursewareElementType.COMPONENT, duplicationContext);
    }

    @Test
    void duplicate_putIdToContext() {
        Component component = new Component()
                .setId(componentId)
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersionExpr);

        UUID activityId = UUID.randomUUID();

        when(componentGateway.findById(any())).thenReturn(Mono.just(component));
        when(componentGateway.findLatestConfig(componentId)).thenReturn(Mono.empty());
        when(activityService.findById(activityId)).thenReturn(Mono.just(new Activity()));
        when(componentGateway.persist(any(Component.class), eq(activityId), eq(CoursewareElementType.ACTIVITY)))
                .thenReturn(Mono.empty());
        when(pluginService.findLatestVersion(pluginId, pluginVersionExpr)).thenReturn(Mono.just("1.0"));
        when(coursewareAssetService.duplicateAssets(eq(componentId), any(), eq(CoursewareElementType.COMPONENT), any(DuplicationContext.class))).thenReturn(Flux.empty());
        DuplicationContext context = new DuplicationContext();

        Component copied = componentService.duplicate(componentId, activityId, CoursewareElementType.ACTIVITY, context).block();

        assertNotNull(copied);
        assertEquals(1, context.getIdsMap().size());
        assertEquals(copied.getId(), context.getIdsMap().get(componentId));
        assertTrue(context.getScenarios().isEmpty());

        verify(coursewareAssetService).duplicateAssets(componentId, copied.getId(), CoursewareElementType.COMPONENT, context);
    }

    @Test
    void duplicate_error() {
        ComponentService componentServiceSpy = Mockito.spy(componentService);
        TestPublisher<Component> componentPublisher = TestPublisher.create();
        componentPublisher.error(new RuntimeException("can't duplicate component"));
        Mockito.doReturn(componentPublisher.mono()).when(componentServiceSpy).duplicateComponent(any(), any(), any());
        UUID activityId = UUID.randomUUID();
        DuplicationContext context = new DuplicationContext();

        assertThrows(RuntimeException.class, () -> componentServiceSpy.duplicate(componentId, activityId, CoursewareElementType.ACTIVITY, context).block());
        assertTrue(context.getIdsMap().isEmpty());
        assertTrue(context.getScenarios().isEmpty());
    }

    @Test
    void duplicate_interactiveComponentNoConfig() {
        TestPublisher<Void> persisted = TestPublisher.create();
        persisted.complete();
        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);

        Component component = new Component()
                .setId(componentId)
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersionExpr);

        UUID interactiveId = UUID.randomUUID();

        when(componentGateway.findById(componentId)).thenReturn(Mono.just(component));
        when(componentGateway.findLatestConfig(componentId)).thenReturn(Mono.empty());
        when(interactiveService.findById(interactiveId)).thenReturn(Mono.just(new Interactive()));
        when(componentGateway.persist(any(Component.class), eq(interactiveId), eq(CoursewareElementType.INTERACTIVE)))
                .thenReturn(persisted.mono());
        when(pluginService.findLatestVersion(pluginId, pluginVersionExpr)).thenReturn(Mono.just("1.0"));
        when(coursewareAssetService.duplicateAssets(eq(componentId), any(), eq(CoursewareElementType.COMPONENT), any(DuplicationContext.class))).thenReturn(Flux.empty());

        Component copied = componentService.duplicate(componentId, interactiveId, CoursewareElementType.INTERACTIVE, new DuplicationContext()).block();

        assertNotNull(copied);
        assertEquals(pluginId, copied.getPluginId());
        assertEquals(pluginVersionExpr, copied.getPluginVersionExpr());
        assertNotEquals(componentId, copied.getId());

        verify(componentGateway, atLeastOnce()).persist(captor.capture(), eq(interactiveId), eq(CoursewareElementType.INTERACTIVE));
        assertEquals(captor.getValue().getId(), copied.getId());
        verify(componentGateway, never()).persist(any(ComponentConfig.class));
        verify(coursewareAssetService).duplicateAssets(eq(componentId), eq(copied.getId()), eq(CoursewareElementType.COMPONENT), any(DuplicationContext.class));
    }

    @Test
    void duplicate_interactiveComponent() {
        TestPublisher<Void> persisted = TestPublisher.create();
        persisted.complete();
        ArgumentCaptor<Component> captor = ArgumentCaptor.forClass(Component.class);

        Component component = new Component()
                .setId(componentId)
                .setPluginId(pluginId)
                .setPluginVersionExpr(pluginVersionExpr);

        ComponentConfig componentConfig = new ComponentConfig()
                .setComponentId(componentId)
                .setConfig(config)
                .setId(UUID.randomUUID());

        UUID interactiveId = UUID.randomUUID();
        DuplicationContext duplicationContext = new DuplicationContext()
                .setNewRootElementId(rootElementId);

        when(componentGateway.findById(any())).thenReturn(Mono.just(component));
        when(componentGateway.findLatestConfig(componentId)).thenReturn(Mono.just(componentConfig));
        when(interactiveService.findById(interactiveId)).thenReturn(Mono.just(new Interactive()));
        when(componentGateway.persist(any(Component.class), eq(interactiveId), eq(CoursewareElementType.INTERACTIVE)))
                .thenReturn(persisted.mono());
        when(componentGateway.persist(any(ComponentConfig.class))).thenReturn(persisted.mono());
        when(pluginService.findLatestVersion(pluginId, pluginVersionExpr)).thenReturn(Mono.just("1.0"));
        when(coursewareAssetService.duplicateAssets(eq(componentId), any(), eq(CoursewareElementType.COMPONENT), any(DuplicationContext.class))).thenReturn(Flux.empty());

        Component copied = componentService.duplicate(componentId, interactiveId, CoursewareElementType.INTERACTIVE, duplicationContext).block();

        assertNotNull(copied);
        assertEquals(pluginId, copied.getPluginId());
        assertEquals(pluginVersionExpr, copied.getPluginVersionExpr());
        assertNotEquals(componentId, copied.getId());

        verify(componentGateway).persist(captor.capture(), eq(interactiveId), eq(CoursewareElementType.INTERACTIVE));
        assertEquals(captor.getValue().getId(), copied.getId());
        verify(componentGateway).persist(any(ComponentConfig.class));
        verify(coursewareAssetService).duplicateAssets(componentId, copied.getId(), CoursewareElementType.COMPONENT, duplicationContext);
    }

    @Disabled
    @Test
    void duplicate_replaceIdsInConfig() {
        ComponentService componentServiceSpy = Mockito.spy(componentService);
        Component component = new Component().setId(componentId);
        Component newComponent = new Component().setId(UUID.randomUUID());
        when(componentGateway.findById(any())).thenReturn(Mono.just(component));
        Mockito.doReturn(Mono.just(newComponent)).when(componentServiceSpy).duplicateComponent(any(), any(), any());
        ComponentConfig componentConfig = new ComponentConfig()
                .setComponentId(componentId)
                .setConfig("{componentId:" + componentId + "}")
                .setId(UUID.randomUUID());
        when(componentGateway.findLatestConfig(componentId)).thenReturn(Mono.just(componentConfig));
        when(componentGateway.persist(any(ComponentConfig.class))).thenReturn(Mono.empty());
        when(coursewareAssetService.duplicateAssets(eq(componentId), any(), eq(CoursewareElementType.COMPONENT), any(DuplicationContext.class))).thenReturn(Flux.empty());

        UUID activityId = UUID.randomUUID();
        DuplicationContext context = new DuplicationContext();

        componentServiceSpy.duplicate(componentId, activityId, CoursewareElementType.ACTIVITY, context).block();
        ArgumentCaptor<ComponentConfig> captor = ArgumentCaptor.forClass(ComponentConfig.class);
        verify(componentGateway).persist(captor.capture());
        assertEquals("{componentId:" + newComponent.getId() + "}", captor.getValue().getConfig());
        assertEquals(newComponent.getId(), context.getIdsMap().get(componentId));
    }

    @Test
    void createManualGradingConfiguration() {
        when(manualGradingConfigurationGateway.persist(any(ManualGradingConfiguration.class))).thenReturn(Flux.just(new Void[]{}));

        ManualGradingConfiguration created = componentService.createManualGradingConfiguration(componentId, 10d)
                .block();

        assertNotNull(created);
        assertEquals(componentId, created.getComponentId());
        assertEquals(Double.valueOf(10), created.getMaxScore());

        verify(manualGradingConfigurationGateway).persist(any(ManualGradingConfiguration.class));
    }

    @Test
    void createManualGradingConfiguration_nullComponentId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class,
                () -> componentService.createManualGradingConfiguration(null, 10d));

        assertNotNull(e);
        assertEquals("componentId is required", e.getMessage());
    }

    @Test
    void createManualGradingConfiguration_nullMaxScore() {
        when(manualGradingConfigurationGateway.persist(any(ManualGradingConfiguration.class))).thenReturn(Flux.just(new Void[]{}));

        ManualGradingConfiguration created = componentService.createManualGradingConfiguration(componentId, null)
                .block();

        assertNotNull(created);
        assertEquals(componentId, created.getComponentId());
        assertNull(created.getMaxScore());
        verify(manualGradingConfigurationGateway).persist(any(ManualGradingConfiguration.class));
    }

    @Test
    void deleteManualGradingConfiguration() {
        ArgumentCaptor<ManualGradingConfiguration> captor = ArgumentCaptor.forClass(ManualGradingConfiguration.class);
        when(manualGradingConfigurationGateway.delete(any(ManualGradingConfiguration.class))).thenReturn(Flux.just(new Void[]{}));

        componentService.deleteManualGradingConfiguration(componentId)
                .singleOrEmpty()
                .block();

        verify(manualGradingConfigurationGateway).delete(captor.capture());

        ManualGradingConfiguration deleted = captor.getValue();

        assertNotNull(deleted);
        assertEquals(componentId, deleted.getComponentId());
    }

    @Test
    void deleteManualGradingConfiguration_nullComponentId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class,
                () -> componentService.deleteManualGradingConfiguration(null));

        assertNotNull(e);
        assertEquals("componentId is required", e.getMessage());
    }

    @Test
    void findManualGradingConfiguration() {
        when(manualGradingConfigurationGateway.find(componentId)).thenReturn(Mono.just(new ManualGradingConfiguration()));

        ManualGradingConfiguration found = componentService.findManualGradingConfiguration(componentId)
                .block();

        assertNotNull(found);

        verify(manualGradingConfigurationGateway).find(componentId);
    }

    @Test
    void findManualGradingConfiguration_nullComponentId() {
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> componentService.findManualGradingConfiguration(null));

        assertNotNull(e);
        assertEquals("componentId is required", e.getMessage());
    }

    @Test
    void duplicateManualGradingConfigurations_notFound() {
        UUID newComponentId = UUID.randomUUID();

        when(manualGradingConfigurationGateway.find(componentId)).thenReturn(Mono.empty());

        ManualGradingConfiguration duplicated = componentService.duplicateManualGradingConfiguration(componentId, newComponentId)
                .block();

        assertNull(duplicated);

        verify(manualGradingConfigurationGateway, never()).persist(any(ManualGradingConfiguration.class));
    }

    @Test
    void duplicateManualGradingConfigurations() {
        ArgumentCaptor<ManualGradingConfiguration> captor = ArgumentCaptor.forClass(ManualGradingConfiguration.class);

        UUID newComponentId = UUID.randomUUID();

        when(manualGradingConfigurationGateway.find(componentId)).thenReturn(Mono.just(new ManualGradingConfiguration()
                .setMaxScore(20d)));

        when(manualGradingConfigurationGateway.persist(any(ManualGradingConfiguration.class))).thenReturn(Flux.just(new Void[]{}));

        ManualGradingConfiguration duplicated = componentService.duplicateManualGradingConfiguration(componentId, newComponentId)
                .block();

        assertNotNull(duplicated);
        assertEquals(newComponentId, duplicated.getComponentId());
        assertEquals(Double.valueOf(20f), duplicated.getMaxScore());

        verify(manualGradingConfigurationGateway).persist(captor.capture());

        ManualGradingConfiguration persisted = captor.getValue();

        assertNotNull(persisted);
        assertEquals(duplicated.getComponentId(), persisted.getComponentId());
        assertEquals(duplicated.getMaxScore(), persisted.getMaxScore());
    }

    @Test
    void findComponentIds_noPluginId() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                                                  () -> componentService.findComponentIds(null).block());

        assertEquals("plugin Id is required", e.getMessage());
    }

    @Test
    void findComponentIds_success() {
        when(componentGateway.findComponentIdsByPluginId(pluginId)).thenReturn(Flux.just(componentId,componentId));

        List<UUID> componentList = componentService.findComponentIds(pluginId).block();

        assertNotNull(componentList);

        verify(componentGateway).findComponentIdsByPluginId(pluginId);
    }

    @Test
    void restoreComponent_test() {
        when(componentGateway.restoreComponent(componentId,
                                               interactiveId,
                                               CoursewareElementType.INTERACTIVE)).thenReturn(Mono.empty());
        when(componentGateway.findById(componentId)).thenReturn(Mono.just(new Component()));
        Component component = componentService.restoreComponent(Arrays.asList(componentId),
                                                                interactiveId,
                                                                CoursewareElementType.INTERACTIVE).blockFirst();

        assertNotNull(component);

        verify(componentGateway).restoreComponent(componentId, interactiveId, CoursewareElementType.INTERACTIVE);
        verify(componentGateway).findById(componentId);
    }

    @Test
    void deleteComponents_test() {
        when(componentGateway.deleteInteractiveComponent(componentId,
                                               interactiveId)).thenReturn(Mono.empty());

        componentService.deleteInteractiveComponents(Arrays.asList(componentId), interactiveId);

        verify(componentGateway).deleteInteractiveComponent(componentId, interactiveId);
    }

    @Test
    void findParentForComponents_test() {
        ParentByComponent parentByComponent = new ParentByComponent()
                .setComponentId(componentId)
                .setParentId(interactiveId)
                .setParentType(CoursewareElementType.INTERACTIVE);

        when(componentGateway.findParentBy(componentId)).thenReturn(Mono.just(parentByComponent));

        List<ParentByComponent> components = componentService.findParentForComponents(Arrays.asList(componentId)).block();
        assertNotNull(components);
        assertEquals(components.get(0).getComponentId(), componentId);
        assertEquals(components.get(0).getParentId(), interactiveId);
        verify(componentGateway).findParentBy(componentId);
    }

    @Test
    void moveComponents_test() {
        when(componentGateway.deleteInteractiveComponent(any(UUID.class), any(UUID.class))).thenReturn(Mono.empty());
        ParentByComponent parent = new ParentByComponent()
                .setParentId(UUID.randomUUID())
                .setParentType(CoursewareElementType.INTERACTIVE)
                .setComponentId(componentId);
        when(componentGateway.findParentBy(componentId)).thenReturn(Mono.just(parent));
        when(componentGateway.restoreComponent(componentId,
                interactiveId,
                CoursewareElementType.INTERACTIVE)).thenReturn(Mono.empty());

        componentService.move(Arrays.asList(componentId), interactiveId, CoursewareElementType.INTERACTIVE);

        verify(componentGateway).findParentBy(componentId);
    }
}
