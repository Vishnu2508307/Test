package com.smartsparrow.courseware.service;

import com.datastax.driver.core.utils.UUIDs;
import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.data.ActivitySummary;
import com.smartsparrow.courseware.data.ConfigurationField;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.payload.AccountPayload;
import com.smartsparrow.iam.service.AccountService;
import com.smartsparrow.plugin.data.PluginFilter;
import com.smartsparrow.plugin.data.PluginSummary;
import com.smartsparrow.plugin.data.PluginType;
import com.smartsparrow.plugin.service.PluginService;
import com.smartsparrow.util.DateFormat;
import com.smartsparrow.workspace.data.ProjectActivity;
import com.smartsparrow.workspace.data.ProjectGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ActivitySummaryServiceTest {

    @InjectMocks
    private ActivitySummaryService activitySummaryService;

    @Mock
    private ProjectGateway projectGateway;

    @Mock
    private AccountService accountService;

    @Mock
    private CoursewareService coursewareService;

    @Mock
    private ActivityService activityService;

    @Mock
    private PluginService pluginService;

    private static final UUID activityId1 = UUIDs.timeBased();
    private static final UUID projectId1 = UUIDs.timeBased();
    private static final UUID projectId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID configId = UUIDs.timeBased();
    private static final UUID pluginId = UUIDs.timeBased();


    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        PluginSummary pluginSummary = getPluginSummary();
        when(activityService.findById(Mockito.any())).thenReturn(Mono.just(new Activity().setId(activityId1)));
        when(activityService.findLatestConfigId(Mockito.any())).thenReturn(Mono.just(configId));
        when(accountService.getAccountPayload((UUID) Mockito.any())).thenReturn(Mono.just(new AccountPayload().setAccountId(accountId)));
        when(projectGateway.findActivities(Mockito.any())).thenReturn(Flux.just(new ProjectActivity().setActivityId(activityId1).setProjectId(projectId1)));
        when(pluginService.fetchById(Mockito.any())).thenReturn(Mono.just(pluginSummary));
        List<PluginFilter> pluginFilterList = new ArrayList<>();
        when(pluginService.fetchPluginFiltersByIdVersionExpr(any(), any())).thenReturn(Mono.just(pluginFilterList));
    }

    @Test
    void testFindActivitiesSummaryForProject() {
        when(coursewareService.fetchConfigurationFields(Mockito.any(), Mockito.anyList())).thenReturn(Flux.just(new ConfigurationField()));

        ActivitySummary activitiesSummaryForProject = activitySummaryService.findActivitiesSummaryForProject(projectId, Collections.emptyList()).blockFirst();
        assertNotNull(activitiesSummaryForProject);
        assertNull(activitiesSummaryForProject.getConfigFields().get(0).getFieldName());
    }

    @Test
    void testFindActivitiesSummaryForProject_noProjectId() {
        IllegalArgumentFault ex =
                assertThrows(IllegalArgumentFault.class, () -> activitySummaryService.findActivitiesSummaryForProject(null, Collections.emptyList()).blockFirst());
        assertEquals("projectId is missing", ex.getMessage());
    }

    @Test
    void testFindActivitiesSummaryForProject_ListOfConfigField() {
        ConfigurationField config1 = new ConfigurationField().setFieldName("Title1").setFieldValue("title1Value");
        ConfigurationField config2 = new ConfigurationField().setFieldName("foo").setFieldValue("bar");
        when(coursewareService.fetchConfigurationFields(Mockito.any(), Mockito.anyList())).thenReturn(Flux.just(config1, config2));

        ActivitySummary activitiesSummaryForProject = activitySummaryService.findActivitiesSummaryForProject(projectId, Collections.emptyList()).blockFirst();
        assertNotNull(activitiesSummaryForProject);
        assertEquals(activityId1, activitiesSummaryForProject.getActivityId());
        assertNotNull(activitiesSummaryForProject.getCreator());
        assertEquals(DateFormat.asRFC1123(configId), activitiesSummaryForProject.getUpdatedAt());
        assertEquals(DateFormat.asRFC1123(activityId1), activitiesSummaryForProject.getCreatedAt());
        assertEquals("Title1", activitiesSummaryForProject.getConfigFields().get(0).getFieldName());
        assertNotEquals("Title1", activitiesSummaryForProject.getConfigFields().get(1).getFieldName());

    }

    void testFindActivitiesSummaryForProject_PluginInfo() {
        when(coursewareService.fetchConfigurationFields(Mockito.any(), Mockito.anyList())).thenReturn(Flux.just(new ConfigurationField()));
        ActivitySummary activitiesSummaryForProject = activitySummaryService.findActivitiesSummaryForProject(projectId, Collections.emptyList()).blockFirst();
        assertNotNull(activitiesSummaryForProject);
        assertEquals(activityId1, activitiesSummaryForProject.getActivityId());
        assertNotNull(activitiesSummaryForProject.getCreator());
        assertEquals(DateFormat.asRFC1123(configId), activitiesSummaryForProject.getUpdatedAt());
        assertEquals(DateFormat.asRFC1123(activityId1), activitiesSummaryForProject.getCreatedAt());
        assertNotNull(activitiesSummaryForProject.getPlugin());
        assertEquals(pluginId,activitiesSummaryForProject.getPlugin().getPluginId());

    }

    private PluginSummary getPluginSummary() {
        return new PluginSummary()
                    .setId(pluginId)
                    .setName("Course")
                    .setType(PluginType.COURSE);
    }

}
