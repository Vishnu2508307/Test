package com.smartsparrow.rtm.message.authorization;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.smartsparrow.courseware.data.CoursewareElementType;
import com.smartsparrow.iam.service.Account;
import com.smartsparrow.iam.service.AuthenticationContext;
import com.smartsparrow.iam.service.PermissionLevel;
import com.smartsparrow.rtm.message.recv.user_content.SharedResourceMessage;

class AllowSharedResourceProjectOwnerTest {

    @InjectMocks
    private AllowSharedResourceProjectOwner allowSharedResourceContributorOrHigher;

    @Mock
    private CoursewareElementAuthorizerService coursewareElementAuthorizerService;

    private AuthenticationContext authenticationContext;
    private SharedResourceMessage sharedResourceMessage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        authenticationContext = mock(AuthenticationContext.class);
        Account account = mock(Account.class);
        sharedResourceMessage = mock(SharedResourceMessage.class);
        when(authenticationContext.getAccount()).thenReturn(account);
    }

    @Test
    void test_contributorPermission() {
        when(coursewareElementAuthorizerService.authorize(authenticationContext, sharedResourceMessage.getResourceId(), CoursewareElementType.ACTIVITY, PermissionLevel.OWNER))
                .thenReturn(Boolean.TRUE);

        assertTrue(allowSharedResourceContributorOrHigher.test(authenticationContext, sharedResourceMessage));
    }

    @Test
    void test_reviewerPermission() {
        when(coursewareElementAuthorizerService.authorize(authenticationContext, sharedResourceMessage.getResourceId(), CoursewareElementType.ACTIVITY, PermissionLevel.REVIEWER))
                .thenReturn(Boolean.FALSE);

        assertFalse(allowSharedResourceContributorOrHigher.test(authenticationContext, sharedResourceMessage));
    }

}