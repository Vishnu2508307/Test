package com.smartsparrow.rtm.message.authorization;

import static com.smartsparrow.rtm.RTMWebSocketTestUtils.mockAuthenticationContext;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.iam.IamTestUtils;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.ingestion.data.IngestionStatus;
import com.smartsparrow.ingestion.data.IngestionSummary;
import com.smartsparrow.ingestion.service.IngestionService;
import com.smartsparrow.rtm.message.recv.ingestion.IngestionRootElementMessage;
import com.smartsparrow.workspace.service.ProjectPermissionService;
import com.smartsparrow.workspace.service.WorkspaceService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

public class AllowProjectRootElementIngestionReviewerOrHigherTest {

    @InjectMocks
    private AllowProjectRootElementIngestionReviewerOrHigher authorizer;

    @Mock
    private ProjectPermissionService projectPermissionService;

    @Mock
    private IngestionRootElementMessage message;

    @Mock
    private IngestionService ingestionService;
    @Mock
    private WorkspaceService workspaceService;

    @Mock
    private ProjectIngestionRootElementAuthorizerService projectIngestionRootElementAuthorizerService;

    private static final UUID rootElementId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();
    private static final UUID ingestionId = UUID.randomUUID();
    private static final UUID projectId = UUID.randomUUID();
    private static final String configFields = "[{ 'key': 'value' },{ 'title': 'titleName' }]";
    private static final UUID creatorId =  UUID.randomUUID();
    private static final String ambrosiaUrl = "https://locationtoingest";
    private static final UUID workspaceId = UUID.randomUUID();

    private final IngestionSummary ingestionSummary = new IngestionSummary()
            .setId(ingestionId)
            .setProjectId(projectId)
            .setIngestionStats(null)
            .setAmbrosiaUrl(ambrosiaUrl)
            .setStatus(IngestionStatus.UPLOADING)
            .setConfigFields(configFields)
            .setCreatorId(creatorId)
            .setRootElementId(rootElementId);

    private AuthenticationContext authenticationContext = IamTestUtils.mockAuthenticationContext(accountId);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        authenticationContext = mockAuthenticationContext(accountId);
        when(message.getRootElementId()).thenReturn(rootElementId);
        when(projectIngestionRootElementAuthorizerService.authorize(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
    }

    @Test
    void test_reviewer() {
        when(projectPermissionService.findHighestPermissionLevel(accountId, projectId))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));

        assertTrue(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_success_forContributor() {
        when(ingestionService.findIngestionSummaryByRootElementId(rootElementId)).thenReturn(Flux.just(ingestionSummary));
        when(workspaceService.findHighestPermissionLevel(accountId, workspaceId)).thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));


        boolean result = authorizer.test(authenticationContext, message);

        assertTrue(result);
    }

    @Test
    void test_success_forOwner() {
        when(ingestionService.findIngestionSummaryByRootElementId(rootElementId)).thenReturn(Flux.just(ingestionSummary));
        when(workspaceService.findHighestPermissionLevel(accountId, workspaceId)).thenReturn(Mono.just(PermissionLevel.OWNER));

        boolean result = authorizer.test(authenticationContext, message);

        assertTrue(result);
    }

    @Test
    void test_noAccess_forReviewer() {
        when(ingestionService.findIngestionSummaryByRootElementId(rootElementId)).thenReturn(Flux.just(ingestionSummary));
        when(workspaceService.findHighestPermissionLevel(accountId, workspaceId)).thenReturn(Mono.just(PermissionLevel.REVIEWER));


        boolean result = authorizer.test(authenticationContext, message);

        assertTrue(result);
    }

    @Test
    void test_ingestionNotFound() {
        when(ingestionService.findIngestionSummaryByRootElementId(rootElementId)).thenReturn(Flux.empty());

        boolean result = authorizer.test(authenticationContext, message);

        assertTrue(result);
    }

    @Test
    void test_noPermission() {
        when(ingestionService.findIngestionSummaryByRootElementId(rootElementId)).thenReturn(Flux.just(ingestionSummary));
        when(workspaceService.findHighestPermissionLevel(accountId, workspaceId)).thenReturn(Mono.empty());
        when(projectIngestionRootElementAuthorizerService.authorize(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);

        boolean result = authorizer.test(authenticationContext, message);

        assertFalse(result);
    }

    @Test
    void test_exception() {
        TestPublisher<UUID> workspacePublisher = TestPublisher.create();
        workspacePublisher.error(new RuntimeException("any exception"));
        when(ingestionService.findIngestionSummaryByRootElementId(rootElementId)).thenReturn(Flux.just(ingestionSummary));
        when(projectIngestionRootElementAuthorizerService.authorize(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);

        boolean result = authorizer.test(authenticationContext, message);

        assertFalse(result);
    }
}
