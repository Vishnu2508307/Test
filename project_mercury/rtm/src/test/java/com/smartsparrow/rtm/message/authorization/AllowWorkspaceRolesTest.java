package com.smartsparrow.rtm.message.authorization;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Sets;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AuthenticationContext;

class AllowWorkspaceRolesTest {

    private AllowWorkspaceRoles allowWorkspaceRoles;

    @Mock
    private AuthenticationContext authenticationContext;
    @Mock
    private Account account;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        allowWorkspaceRoles = new AllowWorkspaceRoles();
        when(authenticationContext.getAccount()).thenReturn(account);
    }

    @Test
    void test_noWorkspaceRole() {
        when(account.getRoles()).thenReturn(Sets.newHashSet(AccountRole.STUDENT, AccountRole.STUDENT_GUEST));

        boolean result = allowWorkspaceRoles.test(authenticationContext, null);

        assertFalse(result);
    }

    @Test
    void test_Developer() {
        when(account.getRoles()).thenReturn(Sets.newHashSet(AccountRole.STUDENT, AccountRole.DEVELOPER));

        boolean result = allowWorkspaceRoles.test(authenticationContext, null);

        assertTrue(result);
    }

    @Test
    void test_AeroInstructor() {
        when(account.getRoles()).thenReturn(Sets.newHashSet(AccountRole.STUDENT_GUEST, AccountRole.AERO_INSTRUCTOR));

        boolean result = allowWorkspaceRoles.test(authenticationContext, null);

        assertTrue(result);
    }

    @Test
    void test_Instructor() {
        when(account.getRoles()).thenReturn(Sets.newHashSet(AccountRole.STUDENT_GUEST, AccountRole.INSTRUCTOR));

        boolean result = allowWorkspaceRoles.test(authenticationContext, null);

        assertFalse(result);
    }

    @Test
    void test_Admin() {
        when(account.getRoles()).thenReturn(Sets.newHashSet(AccountRole.STUDENT_GUEST, AccountRole.ADMIN));

        boolean result = allowWorkspaceRoles.test(authenticationContext, null);

        assertTrue(result);
    }

    @Test
    void test_Support() {
        when(account.getRoles()).thenReturn(Sets.newHashSet(AccountRole.STUDENT_GUEST, AccountRole.SUPPORT));

        boolean result = allowWorkspaceRoles.test(authenticationContext, null);

        assertTrue(result);
    }

}
