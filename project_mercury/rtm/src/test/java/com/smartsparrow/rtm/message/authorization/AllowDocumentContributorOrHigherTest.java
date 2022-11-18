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

public class AllowDocumentContributorOrHigherTest {

    @InjectMocks
    private AllowDocumentContributorOrHigher allowDocumentContributorOrHigher;

    @Mock
    private DocumentPermissionService documentPermissionService;

    private AuthenticationContext authenticationContext;
    private Account account;
    private DocumentMessage message;
    private static final UUID documentId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        authenticationContext = mock(AuthenticationContext.class);
        account = mock(Account.class);
        message = mock(DocumentMessage.class);

        when(account.getId()).thenReturn(UUID.randomUUID());
        when(authenticationContext.getAccount()).thenReturn(account);
        when(message.getDocumentId()).thenReturn(documentId);
    }

    @Test
    void documentIdIsNotFound() {
        when(message.getDocumentId()).thenReturn(null);

        assertFalse(allowDocumentContributorOrHigher.test(authenticationContext, message));
    }

    @Test
    void accountIdIsNotFound() {
        when(authenticationContext.getAccount()).thenReturn(null);

        assertFalse(allowDocumentContributorOrHigher.test(authenticationContext, message));
    }

    @Test
    void permissionLevelNotFound() {
        when(documentPermissionService.findHighestPermissionLevel(account.getId(), documentId))
                .thenReturn(Mono.empty());

        assertFalse(allowDocumentContributorOrHigher.test(authenticationContext, message));
    }

    @Test
    void permissionIsReviewer() {
        when(documentPermissionService.findHighestPermissionLevel(account.getId(), documentId))
                .thenReturn(Mono.just(PermissionLevel.REVIEWER));

        assertFalse(allowDocumentContributorOrHigher.test(authenticationContext, message));
    }

    @Test
    void permissionIsContributor() {
        when(documentPermissionService.findHighestPermissionLevel(account.getId(), documentId))
                .thenReturn(Mono.just(PermissionLevel.CONTRIBUTOR));

        assertTrue(allowDocumentContributorOrHigher.test(authenticationContext, message));
    }

    @Test
    void permissionIsOwner() {
        when(documentPermissionService.findHighestPermissionLevel(account.getId(), documentId))
                .thenReturn(Mono.just(PermissionLevel.OWNER));

        assertTrue(allowDocumentContributorOrHigher.test(authenticationContext, message));
    }
}
