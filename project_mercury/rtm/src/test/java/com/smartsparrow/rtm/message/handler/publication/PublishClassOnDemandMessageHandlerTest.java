package com.smartsparrow.rtm.message.handler.publication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.smartsparrow.cohort.data.CohortSettings;
import com.smartsparrow.cohort.data.CohortSummary;
import com.smartsparrow.cohort.data.EnrollmentType;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.cohort.service.LtiConsumerCredential;
import com.smartsparrow.cohort.wiring.LTIConfig;
import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.ext_http.service.RequestNotification;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.rtm.message.recv.courseware.publication.PublishClassOnDemandMessage;
import com.smartsparrow.util.UUIDs;
import com.smartsparrow.workspace.data.ProjectActivity;
import com.smartsparrow.workspace.data.PublicationSettings;
import com.smartsparrow.workspace.data.Workspace;
import com.smartsparrow.workspace.data.WorkspaceProject;
import com.smartsparrow.workspace.service.ProjectService;
import com.smartsparrow.workspace.service.PublishMetadataService;
import com.smartsparrow.workspace.service.WorkspaceService;
import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.handler.courseware.publication.PublishClassOnDemandMessageHandler;
import com.smartsparrow.workspace.wiring.PublishMetadataConfig;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public class PublishClassOnDemandMessageHandlerTest {
    @InjectMocks
    private PublishClassOnDemandMessageHandler publishClassOnDemandMessageHandler;

    @Mock
    private PublishClassOnDemandMessage message;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private DeploymentService deploymentService;

    @Mock
    private WorkspaceService workspaceService;

    @Mock
    private CohortService cohortService;

    @Mock
    private ActivityService activityService;

    @Mock
    private ProjectService projectService;

    @Mock
    private PublishMetadataService publishMetadataService;

    @Mock
    private LTIConfig ltiConfig;

    @Mock
    private PublishMetadataConfig publishMetadataConfig;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID activityId = UUID.randomUUID();
    private static final String productId = UUID.randomUUID().toString();
    private static final UUID projectId = UUID.randomUUID();
    private static final UUID workspaceId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID cohortId = UUID.randomUUID();
    private static final UUID subscriptionId = UUID.randomUUID();
    private static final LtiConsumerCredential ltiConsumerCredential = new LtiConsumerCredential().setKey("ltiKey").setSecret("ltiSecret");

    private static final String settings = "{"
            + " \"labId\":\"ondemand/product/" + productId + "\""
            + ",\"title\":\"My iLab\""
            + ",\"description\":\"This is my iLab\""
            + ",\"discipline\":\"MasteringBiology\""
            + ",\"estimatedTime\":\"1.5 Hours\""
            + ",\"previewUrl\":\"" + cohortId + "/" + deploymentId + "\""
            + "}";

    private static final String mxLabMetadataUrl = "https://mastering/lab/metadata";

    private Session session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        session = RTMWebSocketTestUtils.mockSession();

        AuthenticationContext authenticationContext = mock(AuthenticationContext.class);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(new Account()
                .setId(accountId));

        when(message.getActivityId()).thenReturn(activityId);
        when(message.getProductId()).thenReturn(productId);
        when(message.getLtiConsumerCredential()).thenReturn(ltiConsumerCredential);
        when(message.getSettings()).thenReturn(settings);

        when(deploymentService.findLatestDeploymentOrEmpty(activityId, deploymentId))
                .thenReturn(Mono.just(new DeployedActivity().setCohortId(cohortId)));

        when(publishMetadataService.publish(any(String.class), any(PublicationSettings.class)))
                        .thenReturn(Mono.just(new RequestNotification()));

        when(activityService.findById(activityId)).thenReturn(Mono.just(new Activity()));
    }

    @Test
    void validate_noActivityId() {
        when(message.getActivityId()).thenReturn(null);
        when(message.getProductId()).thenReturn(productId);
        when(message.getLtiConsumerCredential()).thenReturn(ltiConsumerCredential);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                () -> publishClassOnDemandMessageHandler.validate(message));

        assertEquals("activity id is required", ex.getMessage());
    }

    @Test
    void validate_noProductId() {
        when(message.getActivityId()).thenReturn(activityId);
        when(message.getProductId()).thenReturn(null);
        when(message.getLtiConsumerCredential()).thenReturn(ltiConsumerCredential);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                () -> publishClassOnDemandMessageHandler.validate(message));

        assertEquals("product id is required", ex.getMessage());
    }

    @Test
    void validate_noLtiConsumerCredential() {
        when(message.getActivityId()).thenReturn(activityId);
        when(message.getProductId()).thenReturn(productId);
        when(message.getLtiConsumerCredential()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                () -> publishClassOnDemandMessageHandler.validate(message));

        assertEquals("consumer credential is required", ex.getMessage());
    }

    @Test
    void handle_success_new_publication() throws WriteResponseException {
        when(deploymentService.findProductDeploymentId(productId)).thenReturn(Mono.empty());
        when(deploymentService.deploy(eq(activityId), any(UUID.class), isNull(), eq(true)))
                .thenReturn(Mono.just(new DeployedActivity().setId(deploymentId)));
        when(deploymentService.saveProductDeploymentId(eq(productId), eq(deploymentId)))
                .thenReturn(Mono.empty());

        CohortSummary cohortSummary = new CohortSummary().setId(cohortId).setWorkspaceId(workspaceId);
        when(activityService.findProjectIdByActivity(activityId))
                .thenReturn(Mono.just(new ProjectActivity().setProjectId(projectId).setActivityId(activityId)));
        when(projectService.findWorkspaceIdByProject(projectId))
                .thenReturn(Mono.just(new WorkspaceProject().setProjectId(projectId).setWorkspaceId(workspaceId)));
        when(workspaceService.fetchById(workspaceId)).thenReturn(Mono.just(new Workspace().setId(workspaceId).setSubscriptionId(subscriptionId)));
        JSONObject settings = new JSONObject(message.getSettings());
        when(cohortService.createCohort(any(UUID.class), any(UUID.class), eq(workspaceId), eq(settings.getString("title")+": On-Demand Cohort Template"),
                eq(EnrollmentType.LTI), any(Long.class), nullable(Long.class), eq(subscriptionId)))
                .thenReturn(Mono.just(cohortSummary));
        when(cohortService.createSettings(eq(cohortId), isNull(), isNull(), isNull(), eq(productId)))
                .thenReturn(Mono.just(new CohortSettings()));
        when(cohortService.saveProductCohortId(eq(productId), any(UUID.class)))
                .thenReturn(Mono.empty());
        when(ltiConfig.getKey()).thenReturn("ltiKey");
        when(ltiConfig.getSecret()).thenReturn("ltiSecret");
        when(cohortService.saveLTIConsumerKey(cohortSummary, ltiConfig.getKey(), ltiConfig.getSecret()))
                .thenReturn(Mono.just(cohortSummary));

        when(publishMetadataConfig.getMasteringLabMetadataUrl()).thenReturn(mxLabMetadataUrl);

        publishClassOnDemandMessageHandler.handle(session, message);

        String expectedMessage = "{\"type\":\"publication.classondemand.publish.request.ok\",\"response\":{\"activityId\":\"" + activityId + "\"}}";
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expectedMessage);
    }

    @Test
    void handle_success_existing_publication() throws WriteResponseException {
        when(deploymentService.findProductDeploymentId(productId)).thenReturn(Mono.just(deploymentId));
        when(deploymentService.deploy(eq(activityId), eq(cohortId), eq(deploymentId), eq(true)))
                .thenReturn(Mono.just(new DeployedActivity().setId(deploymentId)));
        when(deploymentService.saveProductDeploymentId(eq(productId), eq(deploymentId)))
                .thenReturn(Mono.empty());

        when(publishMetadataConfig.getMasteringLabMetadataUrl()).thenReturn(mxLabMetadataUrl);

        publishClassOnDemandMessageHandler.handle(session, message);

        String expectedMessage = "{\"type\":\"publication.classondemand.publish.request.ok\",\"response\":{\"activityId\":\"" + activityId + "\"}}";
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expectedMessage);
    }

}
