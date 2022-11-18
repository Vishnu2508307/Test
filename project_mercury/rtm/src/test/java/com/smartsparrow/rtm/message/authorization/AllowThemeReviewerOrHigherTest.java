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

import com.smartsparrow.courseware.service.ThemePermissionService;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.recv.courseware.theme.ThemeMessage;

import reactor.core.publisher.Mono;

class AllowThemeReviewerOrHigherTest {

    @InjectMocks
    private AllowThemeReviewerOrHigher authorizer;

    @Mock
    private ThemePermissionService themePermissionService;
    @Mock
    private ThemeMessage message;

    private static final UUID accountId = UUID.randomUUID();
    private static final UUID themeId = UUID.randomUUID();
    private AuthenticationContext authenticationContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);

        authenticationContext = mock(AuthenticationContext.class);
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(accountId);

        when(message.getThemeId()).thenReturn(themeId);
        when(authenticationContext.getAccount()).thenReturn(account);

    }

    @Test
    void test_permissionNotFound() {
        when(themePermissionService.findHighestPermissionLevel(accountId, themeId)).thenReturn(Mono.empty());

        assertFalse(authorizer.test(authenticationContext, message));
    }

    @Test
    void test_anyPermission() {
        when(themePermissionService.findHighestPermissionLevel(accountId,
                                                               themeId)).thenReturn(Mono.just(PermissionLevel.REVIEWER));
        assertTrue(authorizer.test(authenticationContext, message));
    }

}
