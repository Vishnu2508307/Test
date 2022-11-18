package com.smartsparrow.rtm.message.authorization;

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
import com.smartsparrow.rtm.message.recv.user_content.RecentlyViewedMessage;

import graphql.Assert;

class AllowRecentlyViewedProjectOwnerTest {

    @InjectMocks
    private AllowRecentlyViewedProjectOwner allowRecentlyViewedProjectOwner;

    @Mock
    private CoursewareElementAuthorizerService coursewareElementAuthorizerService;

    private AuthenticationContext authenticationContext;
    private RecentlyViewedMessage recentlyViewedMessage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        authenticationContext = mock(AuthenticationContext.class);
        Account account = mock(Account.class);
        recentlyViewedMessage = mock(RecentlyViewedMessage.class);
        when(authenticationContext.getAccount()).thenReturn(account);
    }

    @Test
    void test_contributorPermission() {
        when(coursewareElementAuthorizerService.authorize(authenticationContext, recentlyViewedMessage.getProjectId(), CoursewareElementType.ACTIVITY, PermissionLevel.OWNER))
                .thenReturn(Boolean.TRUE);

        Assert.assertTrue(allowRecentlyViewedProjectOwner.test(authenticationContext, recentlyViewedMessage));
    }

    @Test
    void test_reviewerPermission() {
        when(coursewareElementAuthorizerService.authorize(authenticationContext, recentlyViewedMessage.getProjectId(), CoursewareElementType.ACTIVITY, PermissionLevel.REVIEWER))
                .thenReturn(Boolean.FALSE);

        Assert.assertFalse(allowRecentlyViewedProjectOwner.test(authenticationContext, recentlyViewedMessage));
    }

}