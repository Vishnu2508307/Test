package com.smartsparrow.graphql.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Sets;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;

class AllowLearnspaceRolesTest {

    @InjectMocks
    private AllowLearnspaceRoles allowLearnspaceRoles;
    @Mock
    private AuthenticationContextProvider authenticationContextProvider;
    @Mock
    private AuthenticationContext authenticationContext;
    @Mock
    private Account account;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
    }

    @Test
    @DisplayName("Should not allow if authenticated user does not have roles")
    void test_noRoles() {
        when(account.getRoles()).thenReturn(null);
        assertFalse(allowLearnspaceRoles.test(authenticationContext));

        when(account.getRoles()).thenReturn(Sets.newHashSet());
        assertFalse(allowLearnspaceRoles.test(authenticationContext));
    }

    @ParameterizedTest
    @EnumSource(value = AccountRole.class)
    @DisplayName("Should allow if authenticated user has any role")
    void test_allowAnyRole(AccountRole role) {
        when(account.getRoles()).thenReturn(Sets.newHashSet(role));

        assertTrue(allowLearnspaceRoles.test(authenticationContext));
    }
}
