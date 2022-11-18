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
import org.mockito.MockitoAnnotations;

import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.ingestion.data.IngestionStatus;
import com.smartsparrow.ingestion.data.IngestionSummary;
import com.smartsparrow.ingestion.service.IngestionService;
import com.smartsparrow.rtm.message.recv.ingestion.IngestionMessage;
import com.smartsparrow.workspace.service.ProjectPermissionService;

import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

class ProjectIngestionAuthorizerServiceTest {
    @InjectMocks
    private ProjectIngestionAuthorizerService authorizer;

    @Mock
    private IngestionService ingestionService;
    @Mock
    private ProjectPermissionService projectPermissionService;

    private AuthenticationContext authenticationContext;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID ingestionId = UUID.randomUUID();
    private static final UUID projectId = UUID.randomUUID();
    private static final String configFields = "[{ 'key': 'value' },{ 'title': 'titleName' }]";
    private static final UUID creatorId =  UUID.randomUUID();
    private static final String ambrosiaUrl = "https://locationtoingest";

    private final IngestionSummary ingestionSummary = new IngestionSummary()
            .setId(ingestionId)
            .setProjectId(projectId)
            .setIngestionStats(null)
            .setAmbrosiaUrl(ambrosiaUrl)
            .setStatus(IngestionStatus.UPLOADING)
            .setConfigFields(configFields)
            .setCreatorId(creatorId);

    @Mock
    private IngestionMessage message;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        authenticationContext = mockAuthenticationContext(accountId);
        when(message.getIngestionId()).thenReturn(ingestionId);
        when(ingestionService.findById(ingestionId)).thenReturn(Mono.just(ingestionSummary));
    }

    @Test
    void test_success_forContributor() {
        when(projectPermissionService.findHighestPermissionLevel(accountId, projectId)).thenReturn(Mono.just(
                PermissionLevel.CONTRIBUTOR));

        boolean result = authorizer.authorize(authenticationContext, message.getIngestionId(), PermissionLevel.CONTRIBUTOR);

        assertTrue(result);
    }

    @Test
    void test_success_forOwner() {
        when(projectPermissionService.findHighestPermissionLevel(accountId, projectId)).thenReturn(Mono.just(PermissionLevel.OWNER));

        boolean result = authorizer.authorize(authenticationContext, message.getIngestionId(), PermissionLevel.OWNER);

        assertTrue(result);
    }

    @Test
    void test_projectNotFound() {

        boolean result = authorizer.authorize(authenticationContext, message.getIngestionId(), PermissionLevel.REVIEWER);

        assertFalse(result);
    }

    @Test
    void test_noPermission() {
        when(projectPermissionService.findHighestPermissionLevel(accountId, projectId)).thenReturn(Mono.empty());

        boolean result = authorizer.authorize(authenticationContext, message.getIngestionId(), PermissionLevel.REVIEWER);

        assertFalse(result);
    }

    @Test
    void test_exception() {
        TestPublisher<UUID> workspacePublisher = TestPublisher.create();
        workspacePublisher.error(new RuntimeException("any runtime exception"));

        boolean result = authorizer.authorize(authenticationContext, message.getIngestionId(), PermissionLevel.REVIEWER);

        assertFalse(result);
    }
}