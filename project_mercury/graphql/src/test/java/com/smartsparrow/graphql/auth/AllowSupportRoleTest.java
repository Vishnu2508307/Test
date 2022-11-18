package com.smartsparrow.graphql.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Sets;
import com.smartsparrow.exception.IllegalArgumentFault;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AccountRole;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.wiring.AuthenticationContextProvider;

class AllowSupportRoleTest {

    @InjectMocks
    private AllowSupportRole allowSupportRole;

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;

    @Mock
    private Account account;

    @Mock
    private AuthenticationContext authenticationContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        when(authenticationContextProvider.get()).thenReturn(authenticationContext);
        when(authenticationContext.getAccount()).thenReturn(account);
    }

    @Test
    void test_noAccount() {
        when(authenticationContext.getAccount()).thenReturn(null);

        IllegalArgumentFault f = assertThrows(IllegalArgumentFault.class, () -> allowSupportRole.test(authenticationContext));

        assertEquals("account is required", f.getMessage());
    }

    @Test
    void test_noRoles() {
        // empty roles
        when(account.getRoles()).thenReturn(new HashSet<>());
        assertFalse(allowSupportRole.test(authenticationContext));

        // null roles
        when(account.getRoles()).thenReturn(null);
        assertFalse(allowSupportRole.test(authenticationContext));
    }

    @Test
    void test_support() {
        // only support role
        Set<AccountRole> roles = Sets.newHashSet(AccountRole.SUPPORT);
        when(account.getRoles()).thenReturn(roles);
        assertTrue(allowSupportRole.test(authenticationContext));

        // multiple roles including support
        roles.add(AccountRole.DEVELOPER);
        assertTrue(allowSupportRole.test(authenticationContext));
    }

    @Test
    void test_anyOtherRole() {
        Set<AccountRole> roles = Arrays.stream(AccountRole.values())
                .filter(one -> !one.equals(AccountRole.SUPPORT))
                .collect(Collectors.toSet());

        when(account.getRoles()).thenReturn(roles);
        assertFalse(allowSupportRole.test(authenticationContext));
    }

}
