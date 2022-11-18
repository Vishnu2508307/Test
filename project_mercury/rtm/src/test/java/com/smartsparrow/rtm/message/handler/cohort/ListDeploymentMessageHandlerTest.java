package com.smartsparrow.rtm.message.handler.cohort;

import static com.smartsparrow.rtm.MessageHandlerTestUtils.verifySentMessage;
import static com.smartsparrow.rtm.message.handler.cohort.ListDeploymentMessageHandler.WORKSPACE_DEPLOYMENT_LIST_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.learner.data.LearnerActivity;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.learner.service.LearnerActivityService;
import com.smartsparrow.plugin.data.PluginFilter;
import com.smartsparrow.plugin.data.PluginFilterType;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.data.PluginType;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.message.recv.cohort.ListDeploymentMessage;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class ListDeploymentMessageHandlerTest {

    @InjectMocks
    private ListDeploymentMessageHandler handler;

    @Mock
    private ListDeploymentMessage message;
    @Mock
    private DeploymentService deploymentService;
    @Mock
    private LearnerActivityService learnerActivityService;
    @Mock
    private PluginService pluginService;
    private Session session;

    private static final UUID cohortId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        session = RTMWebSocketTestUtils.mockSession();

        when(message.getCohortId()).thenReturn(cohortId);
    }

    @Test
    void validate_noCohortId() {
        when(message.getCohortId()).thenReturn(null);

        IllegalArgumentFault t = assertThrows(IllegalArgumentFault.class, () -> handler.validate(message));
        assertEquals("cohortId is required", t.getMessage());
        assertEquals(400, t.getResponseStatusCode());
    }

    @Test
    void handle() throws IOException {
        DeployedActivity deployment1 = new DeployedActivity().setId(UUID.randomUUID()).setActivityId(UUID.randomUUID());
        LearnerActivity activity1 = new LearnerActivity()
                .setId(deployment1.getActivityId())
                .setDeploymentId(deployment1.getId())
                .setPluginId(UUID.randomUUID())
                .setPluginVersionExpr("1.2.0")
                .setConfig("config");
        when(deploymentService.findDeployments(cohortId)).thenReturn(Flux.just(deployment1));
        when(learnerActivityService.findActivity(deployment1.getActivityId(),
                                                 deployment1.getId())).thenReturn(Mono.just(activity1));
        when(pluginService.fetchById(activity1.getPluginId())).thenReturn(Mono.just(new PluginSummary()
                                                                                            .setId(activity1.getPluginId())
                                                                                            .setName("test plugin")
                                                                                            .setType(PluginType.COMPONENT)));

        List<PluginFilter> pluginFilterList = new ArrayList<>();
        PluginFilter pluginFilter = new PluginFilter().setFilterType(PluginFilterType.ID);
        pluginFilterList.add(pluginFilter);

        when(pluginService.fetchPluginFiltersByIdVersionExpr(any(), any())).thenReturn(Mono.just(pluginFilterList));

        handler.handle(session, message);

        verifySentMessage(session, message -> {
            assertEquals(WORKSPACE_DEPLOYMENT_LIST_OK, message.getType());
            assertNotNull(message.getResponse().get("deployments"));
            List deployments = (List) message.getResponse().get("deployments");
            assertEquals(1, deployments.size());
            assertEquals(deployment1.getActivityId().toString(), ((Map) deployments.get(0)).get("activityId"));
            assertEquals(deployment1.getId().toString(), ((Map) deployments.get(0)).get("deploymentId"));
            assertEquals("config", ((Map) deployments.get(0)).get("config"));
            assertNotNull(((Map) deployments.get(0)).get("plugin"));
            assertEquals(activity1.getPluginId().toString(),
                         ((Map) ((Map) deployments.get(0)).get("plugin")).get("pluginId"));
            assertEquals("1.2.0", ((Map) ((Map) deployments.get(0)).get("plugin")).get("version"));
            assertEquals("test plugin", ((Map) ((Map) deployments.get(0)).get("plugin")).get("name"));
            assertEquals("component", ((Map) ((Map) deployments.get(0)).get("plugin")).get("type"));
            assertNotNull((List) ((Map) ((Map) deployments.get(0)).get("plugin")).get("pluginFilters"));
        });
    }

    @Test
    void handle_noDeployments() throws IOException {
        when(deploymentService.findDeployments(cohortId)).thenReturn(Flux.empty());

        handler.handle(session, message);

        verifySentMessage(session, message -> {
            assertEquals(WORKSPACE_DEPLOYMENT_LIST_OK, message.getType());
            assertNotNull(message.getResponse().get("deployments"));
            List deployments = (List) message.getResponse().get("deployments");
            assertEquals(0, deployments.size());
        });
    }

    @Test
    void handle_plugin_filterType_tags() throws IOException {
        DeployedActivity deployment1 = new DeployedActivity().setId(UUID.randomUUID()).setActivityId(UUID.randomUUID());
        LearnerActivity activity1 = new LearnerActivity()
                .setId(deployment1.getActivityId())
                .setDeploymentId(deployment1.getId())
                .setPluginId(UUID.randomUUID())
                .setPluginVersionExpr("1.2.0")
                .setConfig("config");
        when(deploymentService.findDeployments(cohortId)).thenReturn(Flux.just(deployment1));
        when(learnerActivityService.findActivity(deployment1.getActivityId(),
                                                 deployment1.getId())).thenReturn(Mono.just(activity1));
        when(pluginService.fetchById(activity1.getPluginId())).thenReturn(Mono.just(new PluginSummary()
                                                                                            .setId(activity1.getPluginId())
                                                                                            .setName("test plugin")
                                                                                            .setType(PluginType.COMPONENT)));

        List<PluginFilter> pluginFilterList = new ArrayList<>();
        PluginFilter pluginFilter = new PluginFilter().setFilterType(PluginFilterType.TAGS);
        pluginFilterList.add(pluginFilter);

        when(pluginService.fetchPluginFiltersByIdVersionExpr(any(), any())).thenReturn(Mono.just(pluginFilterList));

        handler.handle(session, message);

        verifySentMessage(session, message -> {
            assertEquals(WORKSPACE_DEPLOYMENT_LIST_OK, message.getType());
            assertNotNull(message.getResponse().get("deployments"));
            List deployments = (List) message.getResponse().get("deployments");
            assertEquals(1, deployments.size());
            assertEquals(deployment1.getActivityId().toString(), ((Map) deployments.get(0)).get("activityId"));
            assertEquals(deployment1.getId().toString(), ((Map) deployments.get(0)).get("deploymentId"));
            assertEquals("config", ((Map) deployments.get(0)).get("config"));
            assertNotNull(((Map) deployments.get(0)).get("plugin"));
            assertEquals(activity1.getPluginId().toString(),
                         ((Map) ((Map) deployments.get(0)).get("plugin")).get("pluginId"));
            assertEquals("1.2.0", ((Map) ((Map) deployments.get(0)).get("plugin")).get("version"));
            assertEquals("test plugin", ((Map) ((Map) deployments.get(0)).get("plugin")).get("name"));
            assertEquals("component", ((Map) ((Map) deployments.get(0)).get("plugin")).get("type"));
            assertNotNull((List) ((Map) ((Map) deployments.get(0)).get("plugin")).get("pluginFilters"));
        });
    }
}
