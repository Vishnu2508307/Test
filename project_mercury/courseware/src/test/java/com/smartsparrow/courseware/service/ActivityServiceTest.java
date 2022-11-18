package com.smartsparrow.courseware.service;

import static com.smartsparrow.competency.DocumentDataStubs.ITEM_A;
import static com.smartsparrow.competency.DocumentDataStubs.ITEM_B;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.datastax.driver.core.utils.UUIDs;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.smartsparrow.annotation.service.AnnotationService;
import com.smartsparrow.asset.data.Asset;
import com.smartsparrow.asset.data.AssetMediaType;
import com.smartsparrow.asset.data.MathAsset;
import com.smartsparrow.asset.service.AssetPayload;
import com.smartsparrow.competency.payload.DocumentItemPayload;
import com.smartsparrow.competency.service.DocumentItemService;
import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.ActivityChange;
import com.smartsparrow.courseware.data.ActivityConfig;
import com.smartsparrow.courseware.data.ActivityGateway;
import com.smartsparrow.courseware.data.ActivityTheme;
import com.smartsparrow.courseware.data.ActivityThemeGateway;
import com.smartsparrow.courseware.data.ComponentGateway;
import com.smartsparrow.courseware.data.CoursewareElementDescription;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.DeletedActivity;
import com.smartsparrow.courseware.data.EvaluationMode;
import com.smartsparrow.courseware.data.PathwayGateway;
import com.smartsparrow.courseware.data.WalkablePathwayChildren;
import com.smartsparrow.courseware.lang.ActivityAlreadyExistsFault;
import com.smartsparrow.courseware.lang.ActivityChangeNotFoundException;
import com.smartsparrow.courseware.lang.ActivityNotFoundException;
import com.smartsparrow.courseware.payload.ActivityPayload;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.exception.NotFoundFault;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.plugin.data.PluginFilter;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.lang.PluginNotFoundFault;
import com.smartsparrow.plugin.lang.VersionParserFault;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.workspace.data.ActivityByWorkspace;
import com.smartsparrow.workspace.data.IconLibrary;
import com.smartsparrow.workspace.data.ProjectActivity;
import com.smartsparrow.workspace.data.ProjectGateway;
import com.smartsparrow.workspace.data.ThemePayload;
import com.smartsparrow.workspace.data.ThemeVariant;
import com.smartsparrow.workspace.data.WorkspaceGateway;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;
import reactor.test.publisher.TestPublisher;


class ActivityServiceTest {

    private static final UUID workspaceId = UUID.randomUUID();
    private static final UUID pluginId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID activityId1 = UUIDs.timeBased();
    private static final UUID activityId2 = UUIDs.timeBased();
    private static final UUID activityThemeId = UUID.randomUUID();
    private static final String themeConfig = "Another awesome theme config";
    private static final String versionExpression = "1.*";
    private static final UUID variantId = UUID.randomUUID();
    private static final UUID themeId = UUID.randomUUID();
    private final Activity activity_1 = buildActivity(activityId1);
    private final Activity activity_2 = buildActivity(activityId2);
    private final AccountPayload account = new AccountPayload().setFamilyName("Name1").setAccountId(accountId);
    private final ActivityTheme theme = new ActivityTheme().setId(activityThemeId).setActivityId(activityId1).setConfig(themeConfig);
    private static final UUID projectId1 = UUID.randomUUID();
    private static final UUID projectId2 = UUID.randomUUID();
    private Boolean newDuplicateFlow = false;

    @InjectMocks
    private ActivityService activityService;
    @Mock
    private ActivityGateway activityGateway;
    @Mock
    private PluginService pluginService;
    @Mock
    private AccountService accountService;
    @Mock
    private WorkspaceGateway workspaceGateway;
    @Mock
    private ActivityThemeGateway activityThemeGateway;
    @Mock
    private ComponentGateway componentGateway;
    @Mock
    private PathwayGateway pathwayGateway;
    @Mock
    private CoursewareAssetService coursewareAssetService;
    @Mock
    private DocumentItemService documentItemService;
    @Mock
    private CoursewareElementDescriptionService coursewareDescriptionService;
    @Mock
    private Asset asset;
    @Mock
    private MathAsset mathAsset;
    @Mock
    private ThemeService themeService;
    @Mock
    ProjectGateway projectGateway;
    @Mock
    private AnnotationService annotationService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        when(activityGateway.findById(eq(activityId1))).thenReturn(Mono.just(activity_1));
        when(activityGateway.findById(eq(activityId2))).thenReturn(Mono.just(activity_2));
        when(pluginService.fetchById(eq(pluginId))).thenReturn(Mono.just(new PluginSummary().setName("Plugin 1").setId(
                pluginId)));
        when(activityGateway.findLatestConfig(eq(activityId1))).thenReturn(Mono.just(new ActivityConfig().setId(UUIDs.timeBased()).setConfig(
                "config1")));
        when(activityGateway.findParentPathwayId(activityId1)).thenReturn(Mono.empty());
        when(activityGateway.findChildPathwayIds(activityId1)).thenReturn(Mono.empty());
        when(componentGateway.findComponentIdsByActivity(activityId1)).thenReturn(Flux.empty());
        when(documentItemService.findAllLinked(activityId1)).thenReturn(Flux.empty());
        when(accountService.getAccountPayload(eq(accountId))).thenReturn(Mono.just(account));

        when(activityThemeGateway.findLatestByActivityId(activityId1)).thenReturn(Mono.just(theme));

        when(coursewareAssetService
                     .getAssetPayloads(activityId1)).thenReturn(Mono.empty());
        when(coursewareAssetService
                     .fetchMathAssetsForElement(activityId1)).thenReturn(Mono.empty());
        when(componentGateway.findComponentIdsByActivity(activityId1)).thenReturn(Flux.empty());
        when(coursewareDescriptionService.fetchCoursewareDescriptionByElement(activityId1))
                .thenReturn(Mono.just(new CoursewareElementDescription(pluginId,
                                                                       CoursewareElementType.ACTIVITY,
                                                                       "Activity Description")));
        when(asset.getAssetMediaType()).thenReturn(AssetMediaType.IMAGE);
        List<PluginFilter> pluginFilterList = new ArrayList<>();
        when(pluginService.fetchPluginFiltersByIdVersionExpr(any(), any())).thenReturn(Mono.just(pluginFilterList));
        List<ThemeVariant> variantList = new ArrayList<>();
        ThemeVariant themeVariant = new ThemeVariant()
                .setVariantId(variantId)
                .setVariantName("Day");
        variantList.add(themeVariant);
        when(themeService.fetchThemeByElementId(any()))
                .thenReturn(Mono.just(new ThemePayload().setId(themeId)
                                              .setName("theme_one")
                                              .setThemeVariants(variantList)));
        when(themeService.fetchActivityThemeIconLibraries(any(UUID.class))).thenReturn(Flux.just(new IconLibrary()));
    }

    @Test
    void replaceConfig_noCreatorId() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> activityService.replaceConfig(null, activityId1, "config").block());

        assertEquals("missing account creator", ex.getMessage());
    }

    @Test
    void replaceConfig_noActivityId() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> activityService.replaceConfig(UUID.randomUUID(), null, "config").block());

        assertEquals("missing activity id", ex.getMessage());
    }

    @SuppressWarnings("UnassignedFluxMonoInstance")
    @Test
    void replaceConfig_success() {
        when(activityGateway.findById(activityId1))
                .thenReturn(Mono.just(new Activity()
                        .setPluginId(pluginId)
                        .setPluginVersionExpr(versionExpression)));
        when(activityGateway.persist(any(ActivityConfig.class))).thenReturn(Flux.empty());

        StepVerifier.create(activityService.replaceConfig(UUID.randomUUID(), activityId1, "config")).verifyComplete();

        ArgumentCaptor<ActivityConfig> captor = ArgumentCaptor.forClass(ActivityConfig.class);
        verify(activityGateway).persist(captor.capture());

        assertNotNull(captor.getValue().getId());
        assertEquals(activityId1, captor.getValue().getActivityId());
        assertEquals("config", captor.getValue().getConfig());
    }

    @Test
    void findById() {
        StepVerifier.create(activityService.findById(activityId1)).expectNext(activity_1).verifyComplete();
    }

    @Test
    void findById_notFound() {
        when(activityGateway.findById(eq(activityId1))).thenReturn(Mono.empty());

        StepVerifier.create(activityService.findById(activityId1)).expectError(ActivityNotFoundException.class).verify();
    }

    @Test
    void addToWorkspace() {
        when(workspaceGateway.persist(any(ActivityByWorkspace.class))).thenReturn(Flux.empty());

        activityService.addToWorkspace(activityId1, workspaceId);

        ArgumentCaptor<ActivityByWorkspace> captor = ArgumentCaptor.forClass(ActivityByWorkspace.class);
        verify(workspaceGateway).persist(captor.capture());
        assertEquals(activityId1, captor.getValue().getActivityId());
        assertEquals(workspaceId, captor.getValue().getWorkspaceId());
    }

    @Test
    void getActivityPayload_byActivityId_success() {
        ActivityPayload payload = activityService.getActivityPayload(activityId1).block();

        assertNotNull(payload);
        assertEquals(activityId1, payload.getActivityId());
        assertEquals("config1", payload.getConfig());
        assertEquals(pluginId, payload.getPlugin().getPluginId());
        assertEquals("Plugin 1", payload.getPlugin().getName());
        assertEquals(versionExpression, payload.getPlugin().getVersionExpr());
        assertEquals(account, payload.getCreator());
        assertEquals("Name1", payload.getCreator().getFamilyName());
        assertEquals("Activity Description", payload.getDescription());
        assertNotNull(payload.getCreatedAt());
        assertNotNull(payload.getUpdatedAt());
        assertNull(payload.getAssets());
    }

    @SuppressWarnings("Duplicates")
    @Test
    void getActivityPayload_byActivityId_noConfig() {
        when(activityGateway.findLatestConfig(eq(activityId1))).thenReturn(Mono.empty());

        ActivityPayload payload = activityService.getActivityPayload(activityId1).block();

        assertNotNull(payload);
        assertEquals(activityId1, payload.getActivityId());
        assertNull(payload.getConfig());
        assertNotNull(payload.getPlugin());
        assertNotNull(payload.getCreator());
        assertNotNull(payload.getCreatedAt());
        assertNull(payload.getUpdatedAt());
    }

    @Test
    void getActivityPayload_byActivityId_noActivity() {
        when(activityGateway.findById(eq(activityId1))).thenReturn(Mono.empty());

        assertThrows(ActivityNotFoundException.class, () -> activityService.getActivityPayload(activityId1).block());
    }

    @Test
    void getActivityPayload_byActivityId_exception() {
        when(accountService.getAccountPayload(any(UUID.class))).thenThrow(new IllegalArgumentException("Null parameter"));

        StepVerifier.create(activityService.getActivityPayload(activityId1)).verifyError();
    }

    @Test
    void getActivityPayload_byActivity_success() {
        UUID studentScopeURN = UUID.randomUUID();
        Activity activity1 = new Activity()
                .setId(activityId1)
                .setPluginId(pluginId)
                .setPluginVersionExpr("1")
                .setCreatorId(accountId)
                .setStudentScopeURN(studentScopeURN);

        ActivityPayload payload = activityService.getActivityPayload(activity1).block();

        assertNotNull(payload);
        assertEquals(activityId1, payload.getActivityId());
        assertEquals("config1", payload.getConfig());
        assertEquals(pluginId, payload.getPlugin().getPluginId());
        assertEquals("Plugin 1", payload.getPlugin().getName());
        assertEquals("1", payload.getPlugin().getVersionExpr());
        assertEquals(account, payload.getCreator());
        assertEquals("Name1", payload.getCreator().getFamilyName());
        assertEquals("Activity Description", payload.getDescription());
        assertNotNull(payload.getCreatedAt());
        assertNotNull(payload.getUpdatedAt());
        assertNull(payload.getParentPathwayId());
        assertEquals(0, payload.getChildrenPathways().size());
        assertEquals(0, payload.getComponents().size());
        assertEquals(studentScopeURN, payload.getStudentScopeURN());
        assertNotNull(payload.getThemePayload().getName());
        assertNotNull(payload.getThemePayload().getThemeVariants());
        assertEquals("theme_one", payload.getThemePayload().getName());
        assertEquals(themeId, payload.getThemePayload().getId());
        assertEquals("Day", payload.getThemePayload().getThemeVariants().get(0).getVariantName());
    }

    @SuppressWarnings("Duplicates")
    @Test
    void getActivityPayload_byActivity_noConfig() {
        when(activityGateway.findLatestConfig(eq(activityId1))).thenReturn(Mono.empty());

        ActivityPayload payload = activityService.getActivityPayload(activity_1).block();

        assertNotNull(payload);
        assertEquals(activityId1, payload.getActivityId());
        assertNull(payload.getConfig());
        assertNotNull(payload.getPlugin());
        assertNotNull(payload.getCreator());
        assertNotNull(payload.getCreatedAt());
        assertNull(payload.getUpdatedAt());
    }

    @Test
    void getActivityPayload_byActivity_noPlugin() {
        when(pluginService.fetchById(eq(pluginId))).thenReturn(Mono.empty());

        StepVerifier.create(activityService.getActivityPayload(activity_1))
                .expectError(PluginNotFoundFault.class)
                .verify();
    }

    @Test
    void getActivityPayload_byActivity_exception() {
        when(accountService.getAccountPayload(any(UUID.class))).thenThrow(new IllegalArgumentException("Null parameter"));

        assertThrows(IllegalArgumentException.class, () -> activityService.getActivityPayload(activity_1));
    }

    @Test
    void getActivityPayload_withParentPathway() {
        UUID parentPathwayId = UUID.randomUUID();
        when(activityGateway.findParentPathwayId(activityId1)).thenReturn(Mono.just(parentPathwayId));
        ActivityPayload payload = activityService.getActivityPayload(activity_1).block();

        assertNotNull(payload);
        assertEquals(activityId1, payload.getActivityId());
        assertEquals("config1", payload.getConfig());
        assertEquals(pluginId, payload.getPlugin().getPluginId());
        assertEquals("Plugin 1", payload.getPlugin().getName());
        assertEquals(versionExpression, payload.getPlugin().getVersionExpr());
        assertEquals(accountId, payload.getCreator().getAccountId());
        assertEquals("Name1", payload.getCreator().getFamilyName());
        assertEquals("Activity Description", payload.getDescription());
        assertNotNull(payload.getCreatedAt());
        assertNotNull(payload.getUpdatedAt());
        assertEquals(parentPathwayId, payload.getParentPathwayId());
    }

    @Test
    void getActivityPayload_withParentPathway_withChildren() {
        List<PluginFilter> pluginFilterList = new ArrayList<>();
        UUID parentPathwayId = UUID.randomUUID();
        UUID componentIdOne = UUID.randomUUID();
        UUID componentIdTwo = UUID.randomUUID();
        UUID pathwayIdOne = UUID.randomUUID();
        UUID pathwayIdTwo = UUID.randomUUID();
        when(activityGateway.findParentPathwayId(activityId1)).thenReturn(Mono.just(parentPathwayId));
        when(activityGateway.findChildPathwayIds(activityId1)).thenReturn(Mono.just(Lists.newArrayList(pathwayIdOne, pathwayIdTwo)));
        when(componentGateway.findComponentIdsByActivity(activityId1)).thenReturn(Flux.just(componentIdOne, componentIdTwo));
        when(pluginService.fetchPluginFiltersByIdVersionExpr(pluginId, versionExpression)).thenReturn(Mono.just(pluginFilterList));

        ActivityPayload payload = activityService.getActivityPayload(activity_1).block();

        assertNotNull(payload);
        assertEquals(activityId1, payload.getActivityId());
        assertEquals("config1", payload.getConfig());
        assertEquals(pluginId, payload.getPlugin().getPluginId());
        assertEquals("Plugin 1", payload.getPlugin().getName());
        assertEquals(versionExpression, payload.getPlugin().getVersionExpr());
        assertEquals(accountId, payload.getCreator().getAccountId());
        assertEquals("Name1", payload.getCreator().getFamilyName());
        assertEquals("Activity Description", payload.getDescription());
        assertNotNull(payload.getCreatedAt());
        assertNotNull(payload.getUpdatedAt());
        assertEquals(parentPathwayId, payload.getParentPathwayId());
        assertEquals(2, payload.getChildrenPathways().size());
        assertEquals(2, payload.getComponents().size());
        assertNull(payload.getAssets());
    }

    @Test
    void getActivityPayload_withChildren() {
        UUID componentIdOne = UUID.randomUUID();
        UUID componentIdTwo = UUID.randomUUID();
        UUID pathwayIdOne = UUID.randomUUID();
        UUID pathwayIdTwo = UUID.randomUUID();
        when(activityGateway.findChildPathwayIds(activityId1)).thenReturn(Mono.just(Lists.newArrayList(pathwayIdOne, pathwayIdTwo)));
        when(componentGateway.findComponentIdsByActivity(activityId1)).thenReturn(Flux.just(componentIdOne, componentIdTwo));

        ActivityPayload payload = activityService.getActivityPayload(activity_1).block();

        assertNotNull(payload);
        assertEquals(activityId1, payload.getActivityId());
        assertEquals("config1", payload.getConfig());
        assertEquals("Plugin 1", payload.getPlugin().getName());
        assertEquals(versionExpression, payload.getPlugin().getVersionExpr());
        assertEquals(accountId, payload.getCreator().getAccountId());
        assertEquals("Name1", payload.getCreator().getFamilyName());
        assertEquals("Activity Description", payload.getDescription());
        assertNotNull(payload.getCreatedAt());
        assertNotNull(payload.getUpdatedAt());
        assertNull(payload.getParentPathwayId());
        assertEquals(2, payload.getChildrenPathways().size());
        assertEquals(2, payload.getComponents().size());
    }

    @Test
    void getActivityPayload_withAssets() {
        when(componentGateway.findComponentIdsByActivity(activityId1)).thenReturn(Flux.empty());
        Map<String, String> data = new HashMap<>();
        data.put("url", "http://url");
        AssetPayload assetPayload = new AssetPayload()
                .setUrn("urn1")
                .setAsset(asset)
                .putSource("original", data);
        when(coursewareAssetService.getAssetPayloads(activityId1)).thenReturn(Mono.just(Lists.newArrayList(assetPayload)));

        ActivityPayload payload = activityService.getActivityPayload(activity_1).block();

        assertNotNull(payload);
        assertEquals(1, payload.getAssets().size());
        assertEquals(assetPayload, payload.getAssets().get(0));
    }

    @Test
    void getActivityPayload_withMathAssets() {
        when(componentGateway.findComponentIdsByActivity(activityId1)).thenReturn(Flux.empty());
        Map<String, String> data = new HashMap<>();
        data.put("url", "http://url");
        AssetPayload assetPayload = new AssetPayload()
                .setUrn("urn1")
                .setAsset(mathAsset)
                .putSource("original", data);
        when(coursewareAssetService.fetchMathAssetsForElement(activityId1)).thenReturn(Mono.just(Lists.newArrayList(assetPayload)));

        ActivityPayload payload = activityService.getActivityPayload(activity_1).block();

        assertNotNull(payload);
        assertEquals(1, payload.getMathAssets().size());
        assertEquals(assetPayload, payload.getMathAssets().get(0));
    }

    @Test
    void getActivityPayload_withDocumentItems() {
        when(documentItemService.findAllLinked(any())).thenReturn(Flux.just(ITEM_A, ITEM_B));

        ActivityPayload payload = activityService.getActivityPayload(activityId1).block();

        assertNotNull(payload);
        assertEquals(2, payload.getLinkedDocumentItems().size());
        assertEquals(Sets.newHashSet(DocumentItemPayload.from(ITEM_A), DocumentItemPayload.from(ITEM_B)),
                Sets.newHashSet(payload.getLinkedDocumentItems()));
    }

    @Test
    void create_success_null_configs() throws VersionParserFault {
        UUID creatorId = UUIDs.timeBased();
        UUID pluginId = UUIDs.timeBased();

        PublisherProbe<Void> probe = PublisherProbe.empty();
        when(pluginService.findLatestVersion(eq(pluginId), eq("1.*"))).thenReturn(Mono.just("1.0.0"));
        when(activityGateway.persist(any(Activity.class))).thenReturn(probe.flux());

        StepVerifier.create(activityService.create(creatorId, pluginId, "1.*", null))
                .assertNext(a -> assertAll(() -> {
                    assertNotNull(a.getId());
                    assertEquals(pluginId, a.getPluginId());
                    assertEquals("1.*", a.getPluginVersionExpr());
                    verify(activityGateway, never()).persist(any(ActivityConfig.class));
                    verify(activityThemeGateway, never()).persist(any(ActivityTheme.class));
                }))
                .verifyComplete();
        probe.assertWasNotCancelled();
    }

    @Test
    void create_success() throws VersionParserFault {
        UUID creatorId = UUIDs.timeBased();
        UUID pluginId = UUIDs.timeBased();

        PublisherProbe<Void> probe = PublisherProbe.empty();
        when(pluginService.findLatestVersion(eq(pluginId), eq("1.*"))).thenReturn(Mono.just("1.0.0"));
        when(activityGateway.persist(any(Activity.class))).thenReturn(probe.flux());
        when(activityGateway.findById(any(UUID.class))).thenReturn(Mono.just(new Activity()));
        when(activityThemeGateway.persist(any(ActivityTheme.class))).thenReturn(Mono.empty());
        when(activityGateway.persist(any(ActivityConfig.class))).thenReturn(Flux.empty());

        StepVerifier.create(activityService.create(creatorId, pluginId, "1.*", null))
                .assertNext(a -> assertAll(() -> {
                    assertNotNull(a.getId());
                    assertEquals(pluginId, a.getPluginId());
                    assertEquals("1.*", a.getPluginVersionExpr());
                    assertNotNull(a.getStudentScopeURN());
                    assertEquals(EvaluationMode.DEFAULT, a.getEvaluationMode());
                }))
                .verifyComplete();
        probe.assertWasNotCancelled();
    }

    @Test
    void create_pluginNotFound() throws VersionParserFault {
        UUID creatorId = UUIDs.timeBased();
        UUID pluginId = UUIDs.timeBased();

        PublisherProbe<Void> gatewayReturn = PublisherProbe.empty();
        when(pluginService.findLatestVersion(eq(pluginId), eq("0.*")))
                .thenReturn(Mono.error(new PluginNotFoundFault(pluginId)));
        when(activityGateway.persist(any(Activity.class))).thenReturn(gatewayReturn.flux());

        StepVerifier.create(activityService.create(creatorId, pluginId, "0.*", null))
                .verifyError(PluginNotFoundFault.class);
    }

    @Test
    void create_withParentPathway_pluginNotFound() {
        UUID creatorId = UUIDs.timeBased();
        UUID pluginId = UUIDs.timeBased();
        UUID parentPathwayId = UUIDs.timeBased();
        UUID activityId = UUIDs.timeBased();

        PublisherProbe<Void> gatewayReturn = PublisherProbe.empty();
        when(pluginService.findLatestVersion(eq(pluginId), eq("0.*")))
                .thenReturn(Mono.error(new PluginNotFoundFault(pluginId)));
        when(activityGateway.persist(any(Activity.class))).thenReturn(gatewayReturn.flux());
        when(activityGateway.persistParent(any(UUID.class), any(UUID.class))).thenReturn(gatewayReturn.flux());
        when(activityGateway.findById(activityId)).thenReturn(Mono.empty());

        StepVerifier.create(activityService.create(creatorId, pluginId, parentPathwayId, "0.*", activityId))
                .verifyError(PluginNotFoundFault.class);
    }

    @Test
    void create_withParentPathway_success() {
        UUID creatorId = UUIDs.timeBased();
        UUID pluginId = UUIDs.timeBased();
        UUID parentPathwayId = UUIDs.timeBased();
        UUID activityId = UUIDs.timeBased();

        PublisherProbe<Void> probe = PublisherProbe.empty();
        when(pluginService.findLatestVersion(eq(pluginId), eq("1.*"))).thenReturn(Mono.just("1.0.0"));
        when(activityGateway.persist(any(Activity.class))).thenReturn(probe.flux());
        when(activityGateway.persistParent(any(UUID.class), eq(parentPathwayId))).thenReturn(probe.flux());
        when(activityGateway.findById(activityId)).thenReturn(Mono.empty());
        when(activityThemeGateway.persist(any(ActivityTheme.class))).thenReturn(Mono.empty());
        when(activityGateway.persist(any(ActivityConfig.class))).thenReturn(Flux.empty());
        when(pathwayGateway.persistChild(any(UUID.class), eq(CoursewareElementType.ACTIVITY), eq(parentPathwayId))).thenReturn(Flux.empty());

        StepVerifier.create(activityService.create(creatorId, pluginId, parentPathwayId, "1.*", activityId))
                .assertNext(a -> assertAll(() -> {
                    assertNotNull(a.getId());
                    assertEquals(pluginId, a.getPluginId());
                    assertEquals("1.*", a.getPluginVersionExpr());
                }))
                .verifyComplete();
        probe.assertWasNotCancelled();
    }

    @Test
    void create_idProvidedSuccess() throws VersionParserFault {
        UUID creatorId = UUIDs.timeBased();
        UUID pluginId = UUIDs.timeBased();

        PublisherProbe<Void> probe = PublisherProbe.empty();
        when(pluginService.findLatestVersion(eq(pluginId), eq("1.*"))).thenReturn(Mono.just("1.0.0"));
        when(activityGateway.persist(any(Activity.class))).thenReturn(probe.flux());
        when(activityGateway.findById(eq(activityId1))).thenReturn(Mono.empty());
        when(activityThemeGateway.persist(any(ActivityTheme.class))).thenReturn(Mono.empty());
        when(activityGateway.persist(any(ActivityConfig.class))).thenReturn(Flux.empty());

        StepVerifier.create(activityService.create(creatorId, pluginId, "1.*", activityId1))
                .assertNext(a -> assertAll(() -> {
                    assertNotNull(a.getId());
                    assertEquals(pluginId, a.getPluginId());
                    assertEquals("1.*", a.getPluginVersionExpr());
                    assertNotNull(a.getStudentScopeURN());
                }))
                .verifyComplete();
        probe.assertWasNotCancelled();
    }

    @Test
    void create_idProvidedActivityFound() throws VersionParserFault {
        UUID creatorId = UUIDs.timeBased();
        UUID pluginId = UUIDs.timeBased();

        PublisherProbe<Void> probe = PublisherProbe.empty();
        when(pluginService.findLatestVersion(eq(pluginId), eq("1.*"))).thenReturn(Mono.just("1.0.0"));
        when(activityGateway.persist(any(Activity.class))).thenReturn(probe.flux());
        when(activityGateway.findById(any(UUID.class))).thenReturn(Mono.just(new Activity()));
        when(activityThemeGateway.persist(any(ActivityTheme.class))).thenReturn(Mono.empty());
        when(activityGateway.persist(any(ActivityConfig.class))).thenReturn(Flux.empty());

        assertThrows(ActivityAlreadyExistsFault.class, () -> activityService.create(creatorId, pluginId, "1.*", activityId1).block());
        verify(activityGateway).findById(activityId1);
        verify(activityGateway, never()).persist(any(Activity.class));
    }

    @Test
    void saveRelationship_withIndex() {
        UUID oldChildId = UUID.randomUUID();
        UUID parentPathwayId = UUIDs.timeBased();
        int index = 0;
        WalkablePathwayChildren children = new WalkablePathwayChildren()
                .setPathwayId(parentPathwayId)
                .addWalkable(oldChildId, "ACTIVITY");
        when(activityGateway.persistParent(activityId1, parentPathwayId)).thenReturn(Flux.empty());
        when(pathwayGateway.findWalkableChildren(parentPathwayId)).thenReturn(Mono.just(children));
        when(pathwayGateway.persist(any(WalkablePathwayChildren.class))).thenReturn(Flux.empty());

        activityService.saveRelationship(activityId1, parentPathwayId, index).blockLast();

        ArgumentCaptor<WalkablePathwayChildren> captor = ArgumentCaptor.forClass(WalkablePathwayChildren.class);
        verify(pathwayGateway).persist(captor.capture());
        assertEquals(2, captor.getValue().getWalkableIds().size());
        assertEquals(activityId1, captor.getValue().getWalkableIds().get(0));
        assertEquals(oldChildId, captor.getValue().getWalkableIds().get(1));
        assertEquals(2, captor.getValue().getWalkableTypes().size());
        assertEquals("ACTIVITY", captor.getValue().getWalkableTypes().get(activityId1));
    }

    @Test
    void saveRelationship_indexOutOfBoundsException() {
        UUID oldChildId = UUID.randomUUID();
        UUID parentPathwayId = UUIDs.timeBased();
        int index = 2;
        WalkablePathwayChildren children = new WalkablePathwayChildren()
                .setPathwayId(parentPathwayId)
                .addWalkable(oldChildId, "ACTIVITY");
        when(activityGateway.persistParent(activityId1, parentPathwayId)).thenReturn(Flux.empty());
        when(pathwayGateway.findWalkableChildren(parentPathwayId)).thenReturn(Mono.just(children));
        when(pathwayGateway.persist(any(WalkablePathwayChildren.class))).thenReturn(Flux.empty());

        assertThrows(IndexOutOfBoundsException.class,
                () -> activityService.saveRelationship(activityId1, parentPathwayId, index).blockLast());
    }

    @Test
    void saveRelationship_emptyChildren() {
        UUID parentPathwayId = UUIDs.timeBased();
        int index = 0;
        WalkablePathwayChildren children = new WalkablePathwayChildren()
                .setPathwayId(parentPathwayId);
        when(activityGateway.persistParent(activityId1, parentPathwayId)).thenReturn(Flux.empty());
        when(pathwayGateway.findWalkableChildren(parentPathwayId)).thenReturn(Mono.just(children));
        when(pathwayGateway.persist(any(WalkablePathwayChildren.class))).thenReturn(Flux.empty());

        activityService.saveRelationship(activityId1, parentPathwayId, index).blockLast();

        ArgumentCaptor<WalkablePathwayChildren> captor = ArgumentCaptor.forClass(WalkablePathwayChildren.class);
        verify(pathwayGateway).persist(captor.capture());
        assertEquals(1, captor.getValue().getWalkableIds().size());
        assertEquals(activityId1, captor.getValue().getWalkableIds().get(0));
        assertEquals(1, captor.getValue().getWalkableTypes().size());
    }

    @Test
    void saveRelationship_childrenNotFound() {
        UUID parentPathwayId = UUIDs.timeBased();
        int index = 0;
        when(activityGateway.persistParent(activityId1, parentPathwayId)).thenReturn(Flux.empty());
        when(pathwayGateway.findWalkableChildren(parentPathwayId)).thenReturn(Mono.empty());
        when(pathwayGateway.persist(any(WalkablePathwayChildren.class))).thenReturn(Flux.empty());

        activityService.saveRelationship(activityId1, parentPathwayId, index).blockLast();

        ArgumentCaptor<WalkablePathwayChildren> captor = ArgumentCaptor.forClass(WalkablePathwayChildren.class);
        verify(pathwayGateway).persist(captor.capture());
        assertEquals(1, captor.getValue().getWalkableIds().size());
        assertEquals(activityId1, captor.getValue().getWalkableIds().get(0));
        assertEquals(1, captor.getValue().getWalkableTypes().size());
    }

    @Test
    void create_missingCreatorId() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                activityService.create(null, UUIDs.timeBased(), "nope", null));
        assertEquals("missing account creator", ex.getMessage());
    }

    @Test
    void create_missingPluginId() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                activityService.create(UUIDs.timeBased(), null, "nope", null));
        assertEquals("missing plugin id", ex.getMessage());
    }

    @Test
    void create_missingPluginVersionExpr() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                activityService.create(UUIDs.timeBased(), UUIDs.timeBased(), "", null));
        assertEquals("missing plugin version", ex.getMessage());
    }

    @Test
    void detach_activityIdNotSupplied() {
        UUID parentPathwayId = UUID.randomUUID();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
                activityService.detach(null, parentPathwayId));
        assertEquals("activityId is required", e.getMessage());
    }

    @Test
    void detach_parentPathwayIdNotSupplied() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
                activityService.detach(activityId1, null));
        assertEquals("parentPathwayId is required", e.getMessage());
    }

    @Test
    void detach_success() {
        UUID parentPathwayId = UUID.randomUUID();
        when(activityGateway.removeParent(activityId1)).thenReturn(Flux.just(new Void[]{}));
        when(pathwayGateway.removeChild(activityId1, CoursewareElementType.ACTIVITY, parentPathwayId)).thenReturn(Flux.empty());

        activityService.detach(activityId1, parentPathwayId);

        verify(activityGateway).removeParent(activityId1);
        verify(pathwayGateway).removeChild(activityId1, CoursewareElementType.ACTIVITY, parentPathwayId);
    }

    @Test
    void delete_activityIdNotSupplied() {
        UUID parentPathwayId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
                activityService.delete(null, parentPathwayId, accountId));
        assertEquals("activityId is required", e.getMessage());
    }

    @Test
    void delete_parentPathwayIdNotSupplied() {
        UUID accountId = UUID.randomUUID();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
                activityService.delete(activityId1,null, accountId));
        assertEquals("parentPathwayId is required", e.getMessage());
    }

    @Test
    void delete_accountIdNotSupplied() {
        UUID parentPathwayId = UUID.randomUUID();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
                activityService.delete(activityId1, parentPathwayId,null));
        assertEquals("accountId is required", e.getMessage());
    }

    @Test
    void delete_success() {
        UUID parentPathwayId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        when(activityGateway.removeParent(activityId1)).thenReturn(Flux.just(new Void[]{}));
        when(pathwayGateway.removeChild(activityId1, CoursewareElementType.ACTIVITY, parentPathwayId)).thenReturn(Flux.empty());
        when(activityGateway.persist(any(DeletedActivity.class))).thenReturn(Flux.empty());

        activityService.delete(activityId1, parentPathwayId, accountId);

        verify(activityGateway).removeParent(activityId1);
        verify(pathwayGateway).removeChild(activityId1, CoursewareElementType.ACTIVITY, parentPathwayId);
        verify(activityGateway).persist(any(DeletedActivity.class));
    }

    @Test
    void deleteFromProject_activityIdNotSupplied() {
        UUID projectId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () ->
                activityService.deleteFromProject(null, projectId, accountId));
        assertEquals("activityId is missing", f.getMessage());
    }

    @Test
    void deleteFromProject_projectIdNotSupplied() {
        UUID accountId = UUID.randomUUID();
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () ->
                activityService.deleteFromProject(activityId1, null, accountId));
        assertEquals("projectId is missing", f.getMessage());
    }

    @Test
    void deleteFromProject_accountIdNotSupplied() {
        UUID projectId = UUID.randomUUID();
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () ->
                activityService.deleteFromProject(activityId1, projectId, null));
        assertEquals("accountId is missing", f.getMessage());
    }


    @Test
    void deleteFromProject_success() {
        UUID projectId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        ProjectActivity projectActivity = new ProjectActivity()
                .setActivityId(activityId1)
                .setProjectId(projectId);
        when(projectGateway.delete(projectActivity)).thenReturn(Flux.empty());
        when(annotationService.deleteAnnotationByRootElementId(activityId1)).thenReturn(Flux.just(new Void[]{}));
        when(activityGateway.persist(any(DeletedActivity.class))).thenReturn(Flux.empty());

        activityService.deleteFromProject(activityId1, projectId, accountId);

        verify(projectGateway).delete(projectActivity);
        verify(annotationService).deleteAnnotationByRootElementId(activityId1);
        verify(activityGateway).persist(any(DeletedActivity.class));
    }

    @Test
    void move_success() {
        UUID parentPathwayId = UUID.randomUUID();
        UUID destinationPathwayId = UUID.randomUUID();
        UUID oldChildId = UUID.randomUUID();
        int index = 1;
        WalkablePathwayChildren children = new WalkablePathwayChildren()
                .setPathwayId(destinationPathwayId)
                .addWalkable(oldChildId, "ACTIVITY");

        when(activityGateway.removeParent(activityId1)).thenReturn(Flux.just(new Void[]{}));
        when(pathwayGateway.removeChild(activityId1, CoursewareElementType.ACTIVITY, parentPathwayId)).thenReturn(Flux.empty());
        when(activityGateway.persistParent(activityId1, destinationPathwayId)).thenReturn(Flux.empty());
        when(pathwayGateway.findWalkableChildren(destinationPathwayId)).thenReturn(Mono.just(children));
        when(pathwayGateway.persist(any(WalkablePathwayChildren.class))).thenReturn(Flux.empty());

        activityService.move(activityId1, destinationPathwayId, index, parentPathwayId);

        verify(activityGateway).removeParent(activityId1);
        verify(pathwayGateway).removeChild(activityId1, CoursewareElementType.ACTIVITY, parentPathwayId);
    }

    @Test
    void replaceActivityTheme_noActivityId() {
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> activityService.replaceActivityThemeConfig(null, null).block());
        assertEquals("activity Id is required", e.getMessage());
    }

    @Test
    void replaceActivityTheme_noConfig() {
        ActivityTheme activityTheme = new ActivityTheme()
                .setId(activityThemeId)
                .setActivityId(activityId1)
                .setConfig(null);

        Activity activity = new Activity()
                .setId(activityId1);

        when(activityGateway.findById(any())).thenReturn(Mono.just(activity));
        when(activityThemeGateway.persist(any())).thenReturn(Mono.empty());
        ActivityTheme result = activityService.replaceActivityThemeConfig(activityId1, null).block();

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(activityTheme.getActivityId(), result.getActivityId());
        assertEquals(activityTheme.getConfig(), result.getConfig());

    }

    @Test
    void replaceActivityTheme_withConfig() {
        when(activityThemeGateway.persist(any())).thenReturn(Mono.empty());

        ActivityTheme result = activityService.replaceActivityThemeConfig(activityId1, themeConfig).block();

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(activityId1, result.getActivityId());
        assertEquals(themeConfig, result.getConfig());

    }

    @Test
    void getLatestActivityThemeByActivityId_NullActivityId() {
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> activityService.getLatestActivityThemeByActivityId(null).block());
        assertEquals("activity Id is required", e.getMessage());
    }

    @Test
    void getLatestActivityThemeByActivityId_success() {
        ActivityTheme res = activityService.getLatestActivityThemeByActivityId(activityId1).block();

        assertNotNull(res);
        assertEquals(res.getId(), theme.getId());
        assertEquals(res.getActivityId(), theme.getActivityId());
        assertEquals(res.getConfig(), theme.getConfig());
    }

    @SuppressWarnings("Duplicates")
    @Test
    void duplicateConfig() {
        when(activityGateway.persist(any(ActivityConfig.class))).thenReturn(Flux.just(new Void[]{}));
        UUID oldActivityId = UUID.randomUUID();
        DuplicationContext context = new DuplicationContext();
        context.putIds(oldActivityId, activityId1);

        ActivityConfig duplicated = activityService.duplicateConfig("{id:" + oldActivityId + "}", activityId1, context).block();

        verify(activityGateway, atLeastOnce()).persist(any(ActivityConfig.class));

        assertAll(() -> {
            assertNotNull(duplicated);
            assertEquals(activityId1, duplicated.getActivityId());
            assertEquals("{id:" + activityId1 + "}", duplicated.getConfig());
            assertNotNull(duplicated.getId());
        });
    }

    @SuppressWarnings("Duplicates")
    @Test
    void duplicateTheme() {
        String theme = "a theme";

        when(activityThemeGateway.persist(any(ActivityTheme.class))).thenReturn(Mono.empty());

        ActivityTheme duplicated = activityService.duplicateTheme(theme, activityId1).block();

        verify(activityThemeGateway, atLeastOnce()).persist(any(ActivityTheme.class));

        assertAll(() -> {
            assertNotNull(duplicated);
            assertEquals(activityId1, duplicated.getActivityId());
            assertEquals(theme, duplicated.getConfig());
            assertNotNull(duplicated.getId());
        });
    }

    @Test
    void duplicateActivity_pluginLatestVersionNotfound() {
        Activity activity = buildActivity(activityId1);
        TestPublisher<String> latestVersionPublisher = TestPublisher.create();
        when(pluginService.findLatestVersion(pluginId, versionExpression))
                .thenReturn(latestVersionPublisher.error(new PluginNotFoundFault(pluginId)).mono());
        when(activityGateway.persist(any(Activity.class))).thenReturn(Flux.empty());

        assertThrows(PluginNotFoundFault.class, () -> activityService.duplicateActivity(activity, accountId).block());
        verify(pluginService, atLeastOnce()).findLatestVersion(pluginId, versionExpression);
    }

    @Test
    void duplicateActivity_success() {
        Activity activity = buildActivity(activityId1);
        when(pluginService.findLatestVersion(pluginId, versionExpression)).thenReturn(Mono.just("version"));
        when(activityGateway.persist(any(Activity.class))).thenReturn(Flux.just(new Void[]{}));

        Activity duplicated = activityService.duplicateActivity(activity, accountId).block();

        assertAll(() -> {
            assertNotNull(duplicated);
            assertNotSame(activity.getId(), duplicated.getId());
            assertEquals(activity.getCreatorId(), duplicated.getCreatorId());
            assertEquals(activity.getPluginId(), duplicated.getPluginId());
            assertEquals(activity.getPluginVersionExpr(), duplicated.getPluginVersionExpr());
            assertEquals(activity.getEvaluationMode(), duplicated.getEvaluationMode());
        });

        verify(activityGateway, atLeastOnce()).persist(any(Activity.class));
    }

    @Test
    void findWorkspaceIdByActivity() {
        when(workspaceGateway.findByActivityId(activityId1)).thenReturn(Mono.just(workspaceId));

        StepVerifier.create(activityService.findWorkspaceIdByActivity(activityId1))
                .expectNext(workspaceId)
                .verifyComplete();
    }

    @Test
    void findWorkspaceIdByActivity_noWorkspace() {
        when(workspaceGateway.findByActivityId(activityId1)).thenReturn(Mono.empty());

        StepVerifier.create(activityService.findWorkspaceIdByActivity(activityId1))
                .verifyComplete();
    }

    @Test
    void saveChange_nullActivityId() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> activityService.saveChange(null).block());

        assertEquals("activityId is required", e.getMessage());
    }

    @Test
    void saveChange_error() {
        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.error(new RuntimeException("face_palm"));

        when(activityGateway.persistChange(any(ActivityChange.class))).thenReturn(publisher.flux());

        assertThrows(RuntimeException.class, () -> activityService.saveChange(UUID.randomUUID()).block());

    }

    @Test
    void saveChange_success() {
        UUID activityId = UUID.randomUUID();
        when(activityGateway.persistChange(any(ActivityChange.class))).thenReturn(Flux.just(new Void[]{}));

        ActivityChange activityChange = activityService.saveChange(activityId).block();

        assertNotNull(activityChange);
        assertEquals(activityId, activityChange.getActivityId());
        assertNotNull(activityChange.getChangeId());
    }

    @Test
    void fetchLatestChange_noActivityId() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> activityService.fetchLatestChange(null).block());

        assertEquals("activityId is required", e.getMessage());
    }

    @Test
    void fetchLatestChange_error() {
        UUID activityId = UUID.randomUUID();

        TestPublisher<ActivityChange> publisher = TestPublisher.create();
        publisher.error(new NoSuchElementException());

        when(activityGateway.findLatestActivityChange(any(UUID.class))).thenReturn(publisher.mono());

        ActivityChangeNotFoundException e = assertThrows(ActivityChangeNotFoundException.class,
                () -> activityService.fetchLatestChange(activityId).block());

        assertEquals("activity change not found for activity " + activityId, e.getMessage());
    }

    @Test
    void fetchLatestChange_success() {
        UUID activityId = UUID.randomUUID();

        when(activityGateway.findLatestActivityChange(activityId)).thenReturn(Mono.just(new ActivityChange()));

        ActivityChange activityChange = activityService.fetchLatestChange(activityId).block();

        assertNotNull(activityChange);

        verify(activityGateway).findLatestActivityChange(activityId);
    }

    private Activity buildActivity(UUID activityId) {
        return new Activity()
                .setEvaluationMode(EvaluationMode.DEFAULT)
                .setId(activityId)
                .setPluginId(pluginId)
                .setCreatorId(accountId)
                .setPluginVersionExpr(versionExpression);
    }

    @Test
    void findActivityIds_noPluginId() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                                                  () -> activityService.findActivityIds(null).block());

        assertEquals("plugin Id is required", e.getMessage());
    }

    @Test
    void findActivityIds_success() {
        when(activityGateway.findActivityIdsByPluginId(pluginId)).thenReturn(Flux.just(activityId1,activityId2));

        List<UUID> activityList = activityService.findActivityIds(pluginId).block();

        assertNotNull(activityList);

        verify(activityGateway).findActivityIdsByPluginId(pluginId);
    }
    @Test
    void testHasChildIdsInvalidId() {
        UUID activityId = UUID.randomUUID();
        when(activityGateway.findChildPathwayIds(activityId)).thenReturn(Mono.empty());

        StepVerifier.create(activityService.hasChildPathwayIds(activityId))
                .expectNext(Boolean.FALSE)
                .verifyComplete();

    }

    @Test
    void testHasChildIdsValidId() {
        UUID activityId = UUID.randomUUID();
        UUID pathwayIdOne = UUID.randomUUID();
        UUID pathwayIdTwo = UUID.randomUUID();
        when(activityGateway.findChildPathwayIds(activityId)).thenReturn(Mono.just(Lists.newArrayList(pathwayIdOne, pathwayIdTwo)));
        StepVerifier.create(activityService.hasChildPathwayIds(activityId))
                .expectNext(Boolean.TRUE)
                .verifyComplete();
    }

    @Test
    void updateEvaluationMode_nullInteractiveId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> activityService.updateEvaluationMode(null, EvaluationMode.DEFAULT));

        assertEquals("activityId is required", f.getMessage());
    }

    @Test
    void updateEvaluationMode_nullEvalMode() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> activityService.updateEvaluationMode(activityId1, null));

        assertEquals("evaluationMode is required", f.getMessage());
    }

    @Test
    void updateEvaluationMode() {
        TestPublisher<Void> publisher = TestPublisher.create();
        publisher.complete();

        when(activityGateway.updateEvaluationMode(activityId1, EvaluationMode.COMBINED))
                .thenReturn(publisher.mono());

        activityService.updateEvaluationMode(activityId1, EvaluationMode.COMBINED)
                .block();

        verify(activityGateway).updateEvaluationMode(activityId1, EvaluationMode.COMBINED);
    }

    @Test
    void IsDuplicatedCourseInTheSameProject_invalidCourseId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> activityService.isDuplicatedCourseInTheSameProject(null, projectId1, newDuplicateFlow));

        assertEquals("activityId is required", f.getMessage());
    }

    @Test
    void IsDuplicatedCourseInTheSameProject_invalidProjectId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> activityService.isDuplicatedCourseInTheSameProject(activityId1, null, newDuplicateFlow));

        assertEquals("destinationProjectId is required", f.getMessage());
    }

    @Test
    void isDuplicatedActivityInTheSameProject_invalidActivityId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> activityService.isDuplicatedActivityInTheSameProject(null, activityId2, newDuplicateFlow));

        assertEquals("activityId is required", f.getMessage());
    }

    @Test
    void isDuplicatedActivityInTheSameProject_invalidNewActivityId() {
        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class,
                () -> activityService.isDuplicatedActivityInTheSameProject(activityId1, null, newDuplicateFlow));

        assertEquals("newActivityId is required", f.getMessage());
    }

    @Test
    void IsDuplicatedCourseInTheSameProject_inTheSameProject_newDuplicateFlowIsOn() {
        newDuplicateFlow = true;

        ProjectActivity projectActivity1 = new ProjectActivity()
                .setActivityId(activityId1)
                .setProjectId(projectId1);

        when(projectGateway.findProjectId(activityId1)).thenReturn(Mono.just(projectActivity1));
        Boolean result = activityService.isDuplicatedCourseInTheSameProject(activityId1, projectId1, newDuplicateFlow).block();

        assertEquals(true, result);
    }

    @Test
    void IsDuplicatedCourseInTheSameProject_inTheSameProject_newDuplicateFlowIsOff() {
        ProjectActivity projectActivity1 = new ProjectActivity()
                .setActivityId(activityId1)
                .setProjectId(projectId1);

        when(projectGateway.findProjectId(activityId1)).thenReturn(Mono.just(projectActivity1));
        Boolean result = activityService.isDuplicatedCourseInTheSameProject(activityId1, projectId1, newDuplicateFlow).block();

        assertEquals(true, result);
    }

    @Test
    void IsDuplicatedCourseInTheSameProject_notInTheSameProject_newDuplicateFlowIsOn() {
        newDuplicateFlow = true;

        ProjectActivity projectActivity1 = new ProjectActivity()
                .setActivityId(activityId1)
                .setProjectId(projectId1);

        when(projectGateway.findProjectId(activityId1)).thenReturn(Mono.just(projectActivity1));
        Boolean result = activityService.isDuplicatedCourseInTheSameProject(activityId1, projectId2, newDuplicateFlow).block();

        assertEquals(false, result);
    }

    @Test
    void IsDuplicatedCourseInTheSameProject_notInTheSameProject_newDuplicateFlowIsOff() {
        ProjectActivity projectActivity1 = new ProjectActivity()
                .setActivityId(activityId1)
                .setProjectId(projectId1);

        when(projectGateway.findProjectId(activityId1)).thenReturn(Mono.just(projectActivity1));
        Boolean result = activityService.isDuplicatedCourseInTheSameProject(activityId1, projectId2, newDuplicateFlow).block();

        assertEquals(true, result);
    }

    @Test
    void IsDuplicatedCourseInTheSameProject_cannotFindTheProject_newDuplicateFlowIsOn() {
        newDuplicateFlow = true;

        when(projectGateway.findProjectId(activityId1)).thenReturn(Mono.empty());

        NotFoundFault f = assertThrows(NotFoundFault.class, () ->
                activityService.isDuplicatedCourseInTheSameProject(activityId1, projectId2, newDuplicateFlow).block());

        assertNotNull(f);
        assertEquals("cannot find a project id by activity id: " + activityId1, f.getMessage());
    }

    @Test
    void isDuplicatedActivityInTheSameProject_inTheSameProject_newDuplicateFlowIsOn() {
        newDuplicateFlow = true;

        ProjectActivity projectActivity1 = new ProjectActivity()
                .setActivityId(activityId1)
                .setProjectId(projectId1);

        ProjectActivity projectActivity2 = new ProjectActivity()
                .setActivityId(activityId2)
                .setProjectId(projectId1);

        when(projectGateway.findProjectId(activityId1)).thenReturn(Mono.just(projectActivity1));
        when(projectGateway.findProjectId(activityId2)).thenReturn(Mono.just(projectActivity2));

        Boolean result = activityService.isDuplicatedActivityInTheSameProject(activityId1, activityId2, newDuplicateFlow).block();

        assertEquals(true, result);
    }

    @Test
    void isDuplicatedActivityInTheSameProject_inTheSameProject_newDuplicateFlowIsOff() {
        ProjectActivity projectActivity1 = new ProjectActivity()
                .setActivityId(activityId1)
                .setProjectId(projectId1);

        ProjectActivity projectActivity2 = new ProjectActivity()
                .setActivityId(activityId2)
                .setProjectId(projectId1);

        when(projectGateway.findProjectId(activityId1)).thenReturn(Mono.just(projectActivity1));
        when(projectGateway.findProjectId(activityId2)).thenReturn(Mono.just(projectActivity2));

        Boolean result = activityService.isDuplicatedActivityInTheSameProject(activityId1, activityId2, newDuplicateFlow).block();

        assertEquals(true, result);
    }

    @Test
    void isDuplicatedActivityInTheSameProject_notInTheSameProject_newDuplicateFlowIsOn() {
        newDuplicateFlow = true;

        ProjectActivity projectActivity1 = new ProjectActivity()
                .setActivityId(activityId1)
                .setProjectId(projectId1);

        ProjectActivity projectActivity2 = new ProjectActivity()
                .setActivityId(activityId2)
                .setProjectId(projectId2);

        when(projectGateway.findProjectId(activityId1)).thenReturn(Mono.just(projectActivity1));
        when(projectGateway.findProjectId(activityId2)).thenReturn(Mono.just(projectActivity2));

        Boolean result = activityService.isDuplicatedActivityInTheSameProject(activityId1, activityId2, newDuplicateFlow).block();

        assertEquals(false, result);
    }

    @Test
    void isDuplicatedActivityInTheSameProject_notInTheSameProject_newDuplicateFlowIsOff() {
        ProjectActivity projectActivity1 = new ProjectActivity()
                .setActivityId(activityId1)
                .setProjectId(projectId1);

        ProjectActivity projectActivity2 = new ProjectActivity()
                .setActivityId(activityId2)
                .setProjectId(projectId2);

        when(projectGateway.findProjectId(activityId1)).thenReturn(Mono.just(projectActivity1));
        when(projectGateway.findProjectId(activityId2)).thenReturn(Mono.just(projectActivity2));

        Boolean result = activityService.isDuplicatedActivityInTheSameProject(activityId1, activityId2, newDuplicateFlow).block();

        assertEquals(true, result);
    }

    @Test
    void isDuplicatedActivityInTheSameProject_cannotFindProject1_newDuplicateFlowIsOn() {
        newDuplicateFlow = true;

        when(projectGateway.findProjectId(activityId1)).thenReturn(Mono.empty());
        when(projectGateway.findProjectId(activityId2)).thenReturn(Mono.just(new ProjectActivity()));

        NotFoundFault f = assertThrows(NotFoundFault.class, () ->
                activityService.isDuplicatedActivityInTheSameProject(activityId1, activityId2, newDuplicateFlow).block());

        assertNotNull(f);
        assertEquals("cannot find a project id by activity id: " + activityId1, f.getMessage());
    }

    @Test
    void isDuplicatedActivityInTheSameProject_cannotFindProject2_newDuplicateFlowIsOn() {
        newDuplicateFlow = true;

        when(projectGateway.findProjectId(activityId1)).thenReturn(Mono.just(new ProjectActivity()));
        when(projectGateway.findProjectId(activityId2)).thenReturn(Mono.empty());

        NotFoundFault f = assertThrows(NotFoundFault.class, () ->
                activityService.isDuplicatedActivityInTheSameProject(activityId1, activityId2, newDuplicateFlow).block());

        assertNotNull(f);
        assertEquals("cannot find a project id by activity id: " + activityId2, f.getMessage());
    }
}
