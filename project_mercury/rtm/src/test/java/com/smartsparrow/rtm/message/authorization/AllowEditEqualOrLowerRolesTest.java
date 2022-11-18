package com.smartsparrow.rtm.message.authorization;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Sets;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.rtm.message.recv.iam.EditRoleMessage;
import com.smartsparrow.util.Enums;

class AllowEditEqualOrLowerRolesTest {

    @InjectMocks
    private AllowEditEqualOrLowerRoles allowEditEqualOrLowerRoles;

    @Mock
    private AuthenticationContext authenticationContext;

    @Mock
    private EditRoleMessage message;

    @Mock
    private Account account;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(authenticationContext.getAccount()).thenReturn(account);
        when(account.getId()).thenReturn(UUID.randomUUID());
    }

    @Test
    void test_noRoles() {
        when(account.getRoles()).thenReturn(new HashSet<>());

        assertFalse(allowEditEqualOrLowerRoles.test(authenticationContext, message));
    }

    @Test
    void test_higherRole() {
        Set<AccountRole> roles = Sets.newHashSet(AccountRole.SUPPORT, AccountRole.DEVELOPER);
        when(message.getRole()).thenReturn(Enums.asString(AccountRole.ADMIN));
        when(account.getRoles()).thenReturn(roles);

        assertTrue(allowEditEqualOrLowerRoles.test(authenticationContext, message));
    }

    @Test
    void test_equalRole() {
        Set<AccountRole> roles = Sets.newHashSet(AccountRole.ADMIN, AccountRole.DEVELOPER);
        when(message.getRole()).thenReturn(Enums.asString(AccountRole.ADMIN));
        when(account.getRoles()).thenReturn(roles);

        assertTrue(allowEditEqualOrLowerRoles.test(authenticationContext, message));
    }

    @Test
    void test_lowerRole() {
        Set<AccountRole> roles = Sets.newHashSet(AccountRole.INSTRUCTOR, AccountRole.DEVELOPER);
        when(message.getRole()).thenReturn(Enums.asString(AccountRole.ADMIN));
        when(account.getRoles()).thenReturn(roles);

        assertFalse(allowEditEqualOrLowerRoles.test(authenticationContext, message));
    }

}