package com.smartsparrow.courseware.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.asset.data.AssetProvider;
import com.smartsparrow.asset.data.AssetUrn;
import com.smartsparrow.courseware.data.ActivityConfig;
import com.smartsparrow.courseware.data.ActivityGateway;
import com.smartsparrow.courseware.data.ComponentConfig;
import com.smartsparrow.courseware.data.ComponentGateway;
import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.courseware.data.FeedbackConfig;
import com.smartsparrow.courseware.data.FeedbackGateway;
import com.smartsparrow.courseware.data.InteractiveConfig;
import com.smartsparrow.courseware.data.InteractiveGateway;
import com.smartsparrow.courseware.data.PathwayConfig;
import com.smartsparrow.courseware.data.PathwayGateway;
import com.smartsparrow.util.UUIDs;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class CoursewareAssetConfigServiceTest {

    @InjectMocks
    private CoursewareAssetConfigService coursewareAssetConfigService;

    @Mock
    private ActivityGateway activityGateway;

    @Mock
    private InteractiveGateway interactiveGateway;

    @Mock
    private ComponentGateway componentGateway;

    @Mock
    private FeedbackGateway feedbackGateway;

    @Mock
    private PathwayGateway pathwayGateway;

    private static final UUID elementId = UUIDs.timeBased();

    private static final String alfrescoAssetUrn = new AssetUrn(UUID.randomUUID(), AssetProvider.ALFRESCO).toString();
    private static final String aeroAssetUrn1 = new AssetUrn(UUID.randomUUID(), AssetProvider.AERO).toString();
    private static final String aeroAssetUrn2= new AssetUrn(UUID.randomUUID(), AssetProvider.AERO).toString();

    private static final String alfrescoInteractiveConfig = getInteractiveConfig(alfrescoAssetUrn);
    private static final String aeroInteractiveConfig1 = getInteractiveConfig(aeroAssetUrn1);
    private static final String aeroInteractiveConfig2 = getInteractiveConfig(aeroAssetUrn2);

    private static final String alfrescoComponentConfig = getComponentConfig(alfrescoAssetUrn);
    private static final String aeroComponentConfig1 = getComponentConfig(aeroAssetUrn1);
    private static final String aeroComponentConfig2 = getComponentConfig(aeroAssetUrn2);

    private static final String alfrescoTestConfig = getTestConfig(alfrescoAssetUrn);
    private static final String aeroTestConfig1 = getTestConfig(aeroAssetUrn1);
    private static final String aeroTestConfig2 = getTestConfig(aeroAssetUrn2);

    private ActivityConfig activityConfig;
    private InteractiveConfig interactiveConfig;
    private ComponentConfig componentConfig;
    private FeedbackConfig feedbackConfig;
    private PathwayConfig pathwayConfig;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        when(activityGateway.persist(any(ActivityConfig.class))).thenReturn(Flux.empty());
        when(interactiveGateway.persist(any(InteractiveConfig.class))).thenReturn(Mono.empty());
        when(componentGateway.persist(any(ComponentConfig.class))).thenReturn(Mono.empty());
        when(feedbackGateway.persist(any(FeedbackConfig.class))).thenReturn(Mono.empty());
        when(pathwayGateway.persist(any(PathwayConfig.class))).thenReturn(Flux.empty());

        activityConfig = new ActivityConfig().setActivityId(elementId);
        interactiveConfig = new InteractiveConfig().setInteractiveId(elementId);
        componentConfig = new ComponentConfig().setComponentId(elementId);
        feedbackConfig = new FeedbackConfig().setFeedbackId(elementId);
        pathwayConfig = new PathwayConfig().setPathwayId(elementId);
    }

    @Test
    void updateAssetConfig_interactive_replaceAlfrscoAssetConfig() {
        String newAssetConfig = aeroInteractiveConfig1;

        interactiveConfig.setConfig(alfrescoInteractiveConfig);

        when(interactiveGateway.findLatestConfig(elementId)).thenReturn(Mono.just(interactiveConfig));

        ArgumentCaptor<InteractiveConfig> captor = ArgumentCaptor.forClass(InteractiveConfig.class);

        coursewareAssetConfigService.updateAssetUrn(elementId,
                                                        CoursewareElementType.INTERACTIVE,
                                                        alfrescoAssetUrn,
                                                        aeroAssetUrn1).block();

        verify(interactiveGateway).persist(captor.capture());

        assertNotNull(captor.getValue().getId());
        assertEquals(newAssetConfig, captor.getValue().getConfig());
        assertEquals(elementId, captor.getValue().getInteractiveId());
    }

        @Test
    void updateAssetConfig_interactive_replaceAeroAssetConfig() {
        String newAssetConfig = aeroInteractiveConfig2;

        interactiveConfig.setConfig(aeroInteractiveConfig1);

        when(interactiveGateway.findLatestConfig(elementId)).thenReturn(Mono.just(interactiveConfig));

        ArgumentCaptor<InteractiveConfig> captor = ArgumentCaptor.forClass(InteractiveConfig.class);

        coursewareAssetConfigService.updateAssetUrn(elementId,
                                                        CoursewareElementType.INTERACTIVE,
                                                        aeroAssetUrn1,
                                                        aeroAssetUrn2).block();

        verify(interactiveGateway).persist(captor.capture());

        assertNotNull(captor.getValue().getId());
        assertEquals(newAssetConfig, captor.getValue().getConfig());
        assertEquals(elementId, captor.getValue().getInteractiveId());
    }

    @Test
    void updateAssetConfig_interactive_noReplaceAssetConfig() {
        interactiveConfig.setConfig(alfrescoInteractiveConfig);

        when(interactiveGateway.findLatestConfig(elementId)).thenReturn(Mono.just(interactiveConfig));

        coursewareAssetConfigService.updateAssetUrn(elementId,
                                                        CoursewareElementType.INTERACTIVE,
                                                        aeroAssetUrn1,
                                                        aeroAssetUrn2).block();

        verify(interactiveGateway, never()).persist(any(InteractiveConfig.class));
    }

    @Test
    void updateAssetConfig_interactive_noConfigFound() {
        when(interactiveGateway.findLatestConfig(elementId)).thenReturn(Mono.empty());

        coursewareAssetConfigService.updateAssetUrn(elementId,
                                                        CoursewareElementType.INTERACTIVE,
                                                        aeroAssetUrn1,
                                                        aeroAssetUrn2).block();

        verify(interactiveGateway, never()).persist(any(InteractiveConfig.class));
    }

    @Test
    void updateAssetConfig_component_replaceAlfrscoAssetConfig() {
        String newAssetConfig = aeroComponentConfig1;

        componentConfig.setConfig(alfrescoComponentConfig);

        when(componentGateway.findLatestConfig(elementId)).thenReturn(Mono.just(componentConfig));

        ArgumentCaptor<ComponentConfig> captor = ArgumentCaptor.forClass(ComponentConfig.class);

        coursewareAssetConfigService.updateAssetUrn(elementId,
                                                        CoursewareElementType.COMPONENT,
                                                        alfrescoAssetUrn,
                                                        aeroAssetUrn1).block();

        verify(componentGateway).persist(captor.capture());

        assertNotNull(captor.getValue());
        assertEquals(newAssetConfig, captor.getValue().getConfig());
        assertEquals(elementId, captor.getValue().getComponentId());
    }

    @Test
    void updateAssetConfig_component_replaceAeroAssetConfig() {
        String newAssetConfig = aeroComponentConfig2;

        componentConfig.setConfig(aeroComponentConfig1);

        when(componentGateway.findLatestConfig(elementId)).thenReturn(Mono.just(componentConfig));

        ArgumentCaptor<ComponentConfig> captor = ArgumentCaptor.forClass(ComponentConfig.class);

        coursewareAssetConfigService.updateAssetUrn(elementId,
                                                        CoursewareElementType.COMPONENT,
                                                        aeroAssetUrn1,
                                                        aeroAssetUrn2).block();

        verify(componentGateway).persist(captor.capture());

        assertNotNull(captor.getValue());
        assertEquals(newAssetConfig, captor.getValue().getConfig());
        assertEquals(elementId, captor.getValue().getComponentId());
    }

    @Test
    void updateAssetConfig_component_noReplaceAssetConfig() {
        componentConfig.setConfig(alfrescoComponentConfig);

        when(componentGateway.findLatestConfig(elementId)).thenReturn(Mono.just(componentConfig));

        coursewareAssetConfigService.updateAssetUrn(elementId,
                                                        CoursewareElementType.COMPONENT,
                                                        aeroAssetUrn1,
                                                        aeroAssetUrn2).block();

        verify(componentGateway, never()).persist(any(ComponentConfig.class));
    }

    @Test
    void updateAssetConfig_component_noConfigFound() {
        when(componentGateway.findLatestConfig(elementId)).thenReturn(Mono.empty());

        coursewareAssetConfigService.updateAssetUrn(elementId,
                                                        CoursewareElementType.COMPONENT,
                                                        aeroAssetUrn1,
                                                        aeroAssetUrn2).block();

        verify(componentGateway, never()).persist(any(ComponentConfig.class));
    }

    @Test
    void updateAssetConfig_activity_replaceAlfrscoAssetConfig() {
        String newAssetConfig = aeroTestConfig1;

        activityConfig.setConfig(alfrescoTestConfig);

        when(activityGateway.findLatestConfig(elementId)).thenReturn(Mono.just(activityConfig));

        ArgumentCaptor<ActivityConfig> captor = ArgumentCaptor.forClass(ActivityConfig.class);

        coursewareAssetConfigService.updateAssetUrn(elementId,
                                                        CoursewareElementType.ACTIVITY,
                                                        alfrescoAssetUrn,
                                                        aeroAssetUrn1).block();

        verify(activityGateway).persist(captor.capture());

        assertNotNull(captor.getValue());
        assertEquals(newAssetConfig, captor.getValue().getConfig());
        assertEquals(elementId, captor.getValue().getActivityId());
    }

    @Test
    void updateAssetConfig_activity_replaceAeroAssetConfig() {
        String newAssetConfig = aeroTestConfig2;

        activityConfig.setConfig(aeroTestConfig1);

        when(activityGateway.findLatestConfig(elementId)).thenReturn(Mono.just(activityConfig));

        ArgumentCaptor<ActivityConfig> captor = ArgumentCaptor.forClass(ActivityConfig.class);

        coursewareAssetConfigService.updateAssetUrn(elementId,
                                                        CoursewareElementType.ACTIVITY,
                                                        aeroAssetUrn1,
                                                        aeroAssetUrn2).block();

        verify(activityGateway).persist(captor.capture());

        assertNotNull(captor.getValue());
        assertEquals(newAssetConfig, captor.getValue().getConfig());
        assertEquals(elementId, captor.getValue().getActivityId());

    }

    @Test
    void updateAssetConfig_activity_noReplaceAssetConfig() {
        activityConfig.setConfig(alfrescoTestConfig);

        when(activityGateway.findLatestConfig(elementId)).thenReturn(Mono.just(activityConfig));

        coursewareAssetConfigService.updateAssetUrn(elementId,
                                                        CoursewareElementType.ACTIVITY,
                                                        aeroAssetUrn1,
                                                        aeroAssetUrn2).block();

        verify(activityGateway, never()).persist(any(ActivityConfig.class));
    }

    @Test
    void updateAssetConfig_activity_noConfigFound() {
        when(activityGateway.findLatestConfig(elementId)).thenReturn(Mono.empty());

        coursewareAssetConfigService.updateAssetUrn(elementId,
                                                        CoursewareElementType.ACTIVITY,
                                                        aeroAssetUrn1,
                                                        aeroAssetUrn2).block();

        verify(activityGateway, never()).persist(any(ActivityConfig.class));
    }

    @Test
    void updateAssetConfig_feedback_replaceAlfrscoAssetConfig() {
        String newAssetConfig = aeroTestConfig1;

        feedbackConfig.setConfig(alfrescoTestConfig);

        when(feedbackGateway.findLatestConfig(elementId)).thenReturn(Mono.just(feedbackConfig));

        ArgumentCaptor<FeedbackConfig> captor = ArgumentCaptor.forClass(FeedbackConfig.class);

        coursewareAssetConfigService.updateAssetUrn(elementId,
                                                        CoursewareElementType.FEEDBACK,
                                                        alfrescoAssetUrn,
                                                        aeroAssetUrn1).block();

        verify(feedbackGateway).persist(captor.capture());

        assertNotNull(captor.getValue());
        assertEquals(newAssetConfig, captor.getValue().getConfig());
        assertEquals(elementId, captor.getValue().getFeedbackId());
    }

    @Test
    void updateAssetConfig_feedback_replaceAeroAssetConfig() {
        String newAssetConfig = aeroTestConfig2;

        feedbackConfig.setConfig(aeroTestConfig1);

        when(feedbackGateway.findLatestConfig(elementId)).thenReturn(Mono.just(feedbackConfig));

        ArgumentCaptor<FeedbackConfig> captor = ArgumentCaptor.forClass(FeedbackConfig.class);

        coursewareAssetConfigService.updateAssetUrn(elementId,
                                                        CoursewareElementType.FEEDBACK,
                                                        aeroAssetUrn1,
                                                        aeroAssetUrn2).block();

        verify(feedbackGateway).persist(captor.capture());

        assertNotNull(captor.getValue());
        assertEquals(newAssetConfig, captor.getValue().getConfig());
        assertEquals(elementId, captor.getValue().getFeedbackId());
    }

    @Test
    void updateAssetConfig_feedback_noReplaceAssetConfig() {
        feedbackConfig.setConfig(alfrescoTestConfig);

        when(feedbackGateway.findLatestConfig(elementId)).thenReturn(Mono.just(feedbackConfig));

        coursewareAssetConfigService.updateAssetUrn(elementId,
                                                        CoursewareElementType.FEEDBACK,
                                                        aeroAssetUrn1,
                                                        aeroAssetUrn2).block();

        verify(feedbackGateway, never()).persist(any(FeedbackConfig.class));
    }

    @Test
    void updateAssetConfig_feedback_noConfigFound() {
        when(feedbackGateway.findLatestConfig(elementId)).thenReturn(Mono.empty());

        coursewareAssetConfigService.updateAssetUrn(elementId,
                                                        CoursewareElementType.FEEDBACK,
                                                        aeroAssetUrn1,
                                                        aeroAssetUrn2).block();

        verify(feedbackGateway, never()).persist(any(FeedbackConfig.class));
    }

    @Test
    void updateAssetConfig_pathway_replaceAeroAssetConfig() {
        String newAssetConfig = aeroTestConfig2;

        pathwayConfig.setConfig(aeroTestConfig1);

        when(pathwayGateway.findLatestConfig(elementId)).thenReturn(Mono.just(pathwayConfig));

        ArgumentCaptor<PathwayConfig> captor = ArgumentCaptor.forClass(PathwayConfig.class);

        coursewareAssetConfigService.updateAssetUrn(elementId,
                CoursewareElementType.PATHWAY,
                aeroAssetUrn1,
                aeroAssetUrn2).block();

        verify(pathwayGateway).persist(captor.capture());

        assertNotNull(captor.getValue());
        assertEquals(newAssetConfig, captor.getValue().getConfig());
        assertEquals(elementId, captor.getValue().getPathwayId());
    }

    @Test
    void updateAssetConfig_pathway_noReplaceAssetConfig() {
        pathwayConfig.setConfig(alfrescoTestConfig);

        when(pathwayGateway.findLatestConfig(elementId)).thenReturn(Mono.just(pathwayConfig));

        coursewareAssetConfigService.updateAssetUrn(elementId,
                CoursewareElementType.PATHWAY,
                aeroAssetUrn1,
                aeroAssetUrn2).block();

        verify(pathwayGateway, never()).persist(any(PathwayConfig.class));
    }

    @Test
    void updateAssetConfig_pathway_noConfigFound() {
        when(pathwayGateway.findLatestConfig(elementId)).thenReturn(Mono.empty());

        coursewareAssetConfigService.updateAssetUrn(elementId,
                CoursewareElementType.PATHWAY,
                aeroAssetUrn1,
                aeroAssetUrn2).block();

        verify(pathwayGateway, never()).persist(any(PathwayConfig.class));
    }

    private static String getInteractiveConfig(String assetUrn) {
        return "{\"stage\":{" +
                    "\"sections\": [{" +
                        "\"rows\": [{" +
                            "\"columns\": [{" +
                                "\"stageElements\": [{" +
                                    "\"type\": \"stage-element-image\"," +
                                    "\"config\": { \"url\": \"" + assetUrn + "\"," +
                                        "\"size\": { \"width\": 20 }}," +
                                    "\"height\": 80," +
                                    "\"ref\": \"e5095b10-f487-11eb-b440-f9cec5f49844\"}]," +
                                "\"ref\": \"e3944740-f487-11eb-b440-f9cec5f49844\"}," +
                                "{\"stageElements\": [{" +
                                    "\"type\": \"stage-element-image\"," +
                                    "\"config\": { \"url\": \"urn:alfresco:d7f55690-2146-11ec-9c71-ede0b0352297\"," +
                                        "\"size\": { \"width\": 16 }}," +
                                    "\"height\": 80," +
                                    "\"ref\": \"c7159330-2146-11ec-9da0-d7bc0027f987\"}]," +
                                "\"ref\": \"e3944742-f487-11eb-b440-f9cec5f49844\"}]," +
                            "\"ref\": \"e3944743-f487-11eb-b440-f9cec5f49844\"}]," +
                        "\"rowsAreFullWidth\": false," +
                        "\"ref\": \"e3944744-f487-11eb-b440-f9cec5f49844\",}," +
                        "{\"rows\": [{" +
                            "\"columns\": [{" +
                                "\"stageElements\": [{" +
                                    "\"type\": \"stage-element-image\"," +
                                    "\"config\": { \"url\": \"" + assetUrn + "\"," +
                                        "\"size\": { \"width\": 20 }}," +
                                    "\"height\": 80," +
                                    "\"ref\": \"a337cae0-2147-11ec-9da0-d7bc0027f987\"}]," +
                                "\"ref\": \"a279ab00-2147-11ec-9da0-d7bc0027f987\"}," +
                                "{\"stageElements\": [{" +
                                    "\"type\": \"stage-element-placeholder\"," +
                                    "\"height\": 80," +
                                    "\"ref\": \"a279ab01-2147-11ec-9da0-d7bc0027f987\"}]," +
                                "\"ref\": \"a279ab02-2147-11ec-9da0-d7bc0027f987\"}]," +
                            "\"ref\": \"a279ab03-2147-11ec-9da0-d7bc0027f987\"}]," +
                        "\"rowsAreFullWidth\": false," +
                        "\"ref\": \"a279ab04-2147-11ec-9da0-d7bc0027f987\"}]," +
                    "\"text\": \"\"," +
                    "\"ref\": \"a279ab05-2147-11ec-9da0-d7bc0027f987\"}," +
                "\"screenType\": \"\"," +
                "\"title\": \"Test\" " +
                "}";
    }

    private static String getComponentConfig(String assetUrn){
        return "{" +
                "  \"title\": \"Image carousel\"," +
                "  \"items\": [" +
                "    {" +
                "      \"id\": \"item_517960\"," +
                "      \"image\": \"" + assetUrn + "\"," +
                "      \"caption\": \"<p dir='auto'>1</p>\"," +
                "      \"altText\": \"\"" +
                "    }," +
                "    {" +
                "      \"id\": \"item_58234\"," +
                "      \"image\": \"urn:aero:5cde0680-fbe1-11eb-8b22-8b52e2052ee0\"," +
                "      \"caption\": \"<p dir='auto'>2</p>\"," +
                "      \"altText\": \"\"" +
                "    }" +
                "  ]," +
                "  \"style\": { \"customCss\": \"\", \"cssUrl\": \"\" }" +
                "}";
    }

    private static String getTestConfig(String assetUrn){
        return "{" +
                "  \"title\": \"Test Config\"," +
                "  \"description\": \"\"," +
                "  \"header\": { \"showHeader\": false, \"hideOnScroll\": true }," +
                "  \"image\": \"" + assetUrn + "\"," +
                "  \"progressIndicator\": {" +
                "    \"visibility\": \"hide\"," +
                "    \"position\": \"left\"," +
                "    \"style\": \"screen\"," +
                "    \"backgroundColor\": \"rgba(255, 255, 255, 0.2)\"" +
                "  }," +
                "  \"style\": { \"css\": { \"css\": \"\", \"cssUrl\": \"\" } }," +
                "  \"pathway\": {" +
                "    \"pathwayId\": \"1c519870-0a00-11ec-a793-13f1ceb86aa6\"," +
                "    \"pathwayType\": \"FREE\"" +
                "  }" +
                "}";
    }
}