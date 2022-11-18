package com.smartsparrow.rtm.message.handler.publication;

import com.smartsparrow.cohort.data.CohortSettings;
import com.smartsparrow.cohort.data.CohortSummary;
import com.smartsparrow.cohort.data.EnrollmentType;
import com.smartsparrow.cohort.service.CohortService;
import com.smartsparrow.courseware.data.Activity;
import com.smartsparrow.courseware.service.ActivityService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;
import com.smartsparrow.learner.data.DeployedActivity;
import com.smartsparrow.learner.service.DeploymentService;
import com.smartsparrow.rtm.RTMWebSocketTestUtils;
import com.smartsparrow.rtm.lang.WriteResponseException;
import com.smartsparrow.rtm.message.handler.courseware.publication.PublishPearsonPlusMessageHandler;
import com.smartsparrow.rtm.message.recv.courseware.publication.PublishPearsonPlusMessage;
import com.smartsparrow.workspace.data.Workspace;
import com.smartsparrow.workspace.service.WorkspaceService;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeastOnce;

public class PublishPearsonPlusMessageHandlerTest {

    @InjectMocks
    private PublishPearsonPlusMessageHandler publishPearsonPlusMessageHandler;

    @Mock
    private PublishPearsonPlusMessage message;

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

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID activityId = UUID.randomUUID();
    private static final String productId = UUID.randomUUID().toString();
    private static final UUID workspaceId = UUID.randomUUID();
    private static final UUID deploymentId = UUID.randomUUID();
    private static final UUID cohortId = UUID.randomUUID();
    private static final UUID subscriptionId = UUID.randomUUID();

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

        when(deploymentService.findLatestDeploymentOrEmpty(activityId, deploymentId))
                .thenReturn(Mono.just(new DeployedActivity().setCohortId(cohortId)));

        when(activityService.findById(activityId)).thenReturn(Mono.just(new Activity()));
    }

    @Test
    void validate_noActivityId() {
        when(message.getActivityId()).thenReturn(null);
        when(message.getProductId()).thenReturn(productId);


        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                () -> publishPearsonPlusMessageHandler.validate(message));

        assertEquals("activity id is required", ex.getMessage());
    }

    @Test
    void validate_noProductId() {
        when(message.getActivityId()).thenReturn(activityId);
        when(message.getProductId()).thenReturn(null);

        IllegalArgumentFault ex = assertThrows(IllegalArgumentFault.class,
                () -> publishPearsonPlusMessageHandler.validate(message));

        assertEquals("product id is required", ex.getMessage());
    }

    @Test
    void handle_success_new_publication() throws WriteResponseException {
        when(deploymentService.findProductDeploymentId(productId)).thenReturn(Mono.empty());
        when(deploymentService.deploy(eq(activityId), any(UUID.class), isNull(), eq(true)))
                .thenReturn(Mono.just(new DeployedActivity().setId(deploymentId)));
        when(deploymentService.saveProductDeploymentId(eq(productId), eq(deploymentId)))
                .thenReturn(Mono.empty());

        CohortSummary cohortSummary = new CohortSummary().setId(cohortId).setWorkspaceId(workspaceId);
        when(activityService.findWorkspaceIdByActivity(activityId)).thenReturn(Mono.just(workspaceId));
        when(workspaceService.fetchById(workspaceId)).thenReturn(Mono.just(new Workspace().setId(workspaceId).setSubscriptionId(subscriptionId)));
        when(cohortService.createCohort(any(UUID.class), any(UUID.class), eq(workspaceId), eq("Pearson Plus Cohort"),
                eq(EnrollmentType.OPEN), nullable(Long.class), nullable(Long.class), eq(subscriptionId)))
                .thenReturn(Mono.just(cohortSummary));
        when(cohortService.createSettings(eq(cohortId), isNull(), isNull(), isNull(), eq(productId)))
                .thenReturn(Mono.just(new CohortSettings()));

        when(cohortService.saveProductCohortId(eq(productId), any(UUID.class)))
                .thenReturn(Mono.empty());

        publishPearsonPlusMessageHandler.handle(session, message);

        String expectedMessage = "{\"type\":\"publication.pearsonplus.publish.request.ok\",\"response\":{\"activityId\":\"" + activityId + "\"}}";
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expectedMessage);
    }

    @Test
    void handle_success_existing_publication() throws WriteResponseException {
        when(deploymentService.findProductDeploymentId(productId)).thenReturn(Mono.just(deploymentId));
        when(deploymentService.deploy(eq(activityId), eq(cohortId), eq(deploymentId), eq(true)))
                .thenReturn(Mono.just(new DeployedActivity()));

        publishPearsonPlusMessageHandler.handle(session, message);

        String expectedMessage = "{\"type\":\"publication.pearsonplus.publish.request.ok\",\"response\":{\"activityId\":\"" + activityId + "\"}}";
        verify(session.getRemote(), atLeastOnce()).sendStringByFuture(expectedMessage);
    }
}
