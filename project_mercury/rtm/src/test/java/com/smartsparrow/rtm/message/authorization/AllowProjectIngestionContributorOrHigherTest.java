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

import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.message.recv.ingestion.IngestionMessage;

import reactor.test.publisher.TestPublisher;

class AllowProjectIngestionContributorOrHigherTest {

    @InjectMocks
    private AllowProjectIngestionContributorOrHigher authorizer;
    @Mock
    private ProjectIngestionAuthorizerService projectIngestionAuthorizerService;

    private AuthenticationContext authenticationContext;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID ingestionId = UUID.randomUUID();

    @Mock
    private IngestionMessage message;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        authenticationContext = mockAuthenticationContext(accountId);
        when(message.getIngestionId()).thenReturn(ingestionId);
        when(projectIngestionAuthorizerService.authorize(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
    }

    @Test
    void test_success_forContributor() {

        boolean result = authorizer.test(authenticationContext, message);

        assertTrue(result);
    }

    @Test
    void test_success_forOwner() {

        boolean result = authorizer.test(authenticationContext, message);

        assertTrue(result);
    }

    @Test
    void test_noAccess_forReviewer() {
        when(projectIngestionAuthorizerService.authorize(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);


        boolean result = authorizer.test(authenticationContext, message);

        assertFalse(result);
    }

    @Test
    void test_noPermission() {
        when(projectIngestionAuthorizerService.authorize(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);

        boolean result = authorizer.test(authenticationContext, message);

        assertFalse(result);
    }

    @Test
    void test_exception() {
        TestPublisher<UUID> workspacePublisher = TestPublisher.create();
        workspacePublisher.error(new RuntimeException("any runtime exception"));
        when(projectIngestionAuthorizerService.authorize(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);

        boolean result = authorizer.test(authenticationContext, message);

        assertFalse(result);
    }
}