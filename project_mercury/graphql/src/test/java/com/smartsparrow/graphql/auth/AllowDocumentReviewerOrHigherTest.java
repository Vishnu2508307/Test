package com.smartsparrow.graphql.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.competency.data.Document;
import com.smartsparrow.competency.service.DocumentPermissionService;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;

import reactor.core.publisher.Mono;

public class AllowDocumentReviewerOrHigherTest {

    @InjectMocks
    AllowDocumentReviewerOrHigher allowDocumentReviewerOrHigher;

    @Mock
    AuthenticationContextProvider authenticationContextProvider;

    @Mock
    AuthenticationContext authenticationContext;

    @Mock
    Account account;

    @Mock
    DocumentPermissionService documentPermissionService;

    private UUID documentId1 = UUID.randomUUID();
    private UUID documentId2 = UUID.randomUUID();

    private Document document1 = new Document()
            .setId(documentId1);

    private Document document2 = new Document()
            .setId(documentId2);


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
    }

    @Test
    void test_RoleNotAvailable() {
        when(authenticationContext.getAccount()).thenReturn(null);
        assertThrows(IllegalArgumentFault.class, () -> allowDocumentReviewerOrHigher.test(authenticationContext,documentId1));
    }

    @Test
    void test_user_noPermission() {
        Account account = new Account()
                .setId(UUID.randomUUID());
        when(authenticationContext.getAccount()).thenReturn(account);
        when(documentPermissionService.findHighestPermissionLevel(any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.empty());

        assertFalse(allowDocumentReviewerOrHigher.test(authenticationContext,documentId1));
    }

    @Test
    void test_user_noPermissionForDocumentList() {
        Account account = new Account()
                .setId(UUID.randomUUID());
        when(authenticationContext.getAccount()).thenReturn(account);
        when(documentPermissionService.findHighestPermissionLevel(any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.empty());

        assertFalse(allowDocumentReviewerOrHigher.test(authenticationContext,Arrays.asList(document1, document2)));
    }

    @ParameterizedTest
    @EnumSource(PermissionLevel.class)
    void test_hasPermissions(PermissionLevel permissionLevel) {
        Account account = new Account()
                .setId(UUID.randomUUID());
        when(authenticationContext.getAccount()).thenReturn(account);
        when(documentPermissionService.findHighestPermissionLevel(any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.just(permissionLevel));

        assertTrue(allowDocumentReviewerOrHigher.test(authenticationContext,documentId1));
    }

    @ParameterizedTest
    @EnumSource(PermissionLevel.class)
    void test_hasPermissionsList(PermissionLevel permissionLevel) {
        Account account = new Account()
                .setId(UUID.randomUUID());
        when(authenticationContext.getAccount()).thenReturn(account);
        when(documentPermissionService.findHighestPermissionLevel(any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.just(permissionLevel));

        assertTrue(allowDocumentReviewerOrHigher.test(authenticationContext,Arrays.asList(document1, document2)));
    }

}
