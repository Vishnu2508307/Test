package com.smartsparrow.graphql.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Sets;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;

public class AllowWorkspaceRolesTest {

    @InjectMocks
    private AllowWorkspaceRoles allowWorkspaceRoles;

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
    void test_AccountNotAvailable() {
        when(authenticationContext.getAccount()).thenReturn(null);
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> allowWorkspaceRoles.test(authenticationContext));
        assertEquals("account is required", e.getMessage());
    }

    @Test
    void test_RoleNotAvailable() {
        when(account.getRoles()).thenReturn(null);
        IllegalArgumentFault e = assertThrows(IllegalArgumentFault.class, () -> allowWorkspaceRoles.test(authenticationContext));
        assertEquals("roles can not be empty or null", e.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = AccountRole.class, mode = EnumSource.Mode.EXCLUDE, names = {"STUDENT", "STUDENT_GUEST", "INSTRUCTOR"})
    void test_aero_instructor_withPermission(AccountRole role) {
        when(account.getRoles()).thenReturn(Sets.newHashSet(role));

        assertTrue(allowWorkspaceRoles.test(authenticationContext));
    }

    @ParameterizedTest
    @EnumSource(value = AccountRole.class, mode = EnumSource.Mode.EXCLUDE, names = {"STUDENT", "STUDENT_GUEST", "AERO_INSTRUCTOR", "DEVELOPER", "ADMIN", "SUPPORT"})
    void test_instructor_withPermission(AccountRole role) {
        when(account.getRoles()).thenReturn(Sets.newHashSet(role));

        assertFalse(allowWorkspaceRoles.test(authenticationContext));
    }

}
