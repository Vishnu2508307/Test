package com.smartsparrow.rtm.message.authorization;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.competency.service.DocumentPermissionService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.recv.competency.DocumentMessage;

import reactor.core.publisher.Mono;

class AllowDocumentReviewerOrHigherTest {

    @InjectMocks
    private AllowDocumentReviewerOrHigher authorizer;

    @Mock
    private DocumentPermissionService documentPermissionService;

    private DocumentMessage message;
    private AuthenticationContext authenticationContext;
    private static final UUID documentId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        message = mock(DocumentMessage.class);
        authenticationContext = mock(AuthenticationContext.class);

        when(message.getDocumentId()).thenReturn(documentId);
        when(authenticationContext.getAccount()).thenReturn(new Account().setId(accountId));
    }

    @Test
    void test_permissionLevelNotFound() {
        when(documentPermissionService.findHighestPermissionLevel(accountId, documentId))
                .thenReturn(Mono.empty());

        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_permissionLevelFound() {
        when(documentPermissionService.findHighestPermissionLevel(accountId, documentId))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));

        assertTrue(authorizer.test(authenticationContext, message));
    }

}
